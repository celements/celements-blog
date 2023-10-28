import CelDataRenderer from '/file/resources/celDynJS/celData/cel-data-renderer.mjs?ver=20231026';

const tagName = 'blog-viewer';

function safeAssign(obj, newProps) {
  Object.entries(newProps ?? {})
    .filter(([key, ]) => obj.hasOwnProperty(key))
    .map(([key, value]) => (Array.isArray(obj[key]) && !Array.isArray(value)) 
        ? [key, [value].filter(Boolean)] : [key, value])
    .forEach(([key, value]) => obj[key] = value);
  return obj;
}

export class BlogFilter {
  searchTerm;
  fromPublishDate;
}

export class BlogParams {
  showFields = [];
  maxImageHeight;
  maxImageWidth;
  debug;
}

export default class BlogViewer {
  #resource;
  #filter = new BlogFilter();
  #params = new BlogParams();
  #sortFields = [];
  #abortController;

  constructor(origin, blog) {
    this.#resource = origin + '/' + blog.split('.').join('/');
    console.debug('BlogViewer init', this.#resource);
  }

  get resource() {
    return this.#resource;
  }

  set filter(newPropValue) {
    safeAssign(this.#filter, newPropValue);
  }

  get params() {
    return this.#params;
  }

  set params(newPropValue) {
    safeAssign(this.#params, newPropValue);
  }

  get sortFields() {
    return this.#sortFields.filter(Boolean);
  }

  set sortFields(value) {
    if (Array.isArray(value)) {
      this.#sortFields = value;
    } else {
      throw new TypeError("sortFields must be an array");
    }
  }

  get filter() {
    return this.#filter;
  }

  abort() {
    this.#abortController?.abort();
  }

  async getPage(page, size) {
    this.abort();
    const request = this.#buildRequest(page, size);
    try {
      const response = await fetch(request);
      response.ok || console.error('fetch failed', response);
      const data = response.ok ? await response.json() : {};
      return {
        results: data.results || [],
        counts: data.searchInfo?.hitCount || {},
      };
    } catch (error) {
      if (error.name === 'AbortError') {
        console.info('aborted', request);
        return { results: [], counts: {} };
      } else {
        throw error;
      }
    }
  }

  #buildRequest(page, size) {
    const nb = Math.max(size, 1);
    const start = Math.max(page - 1, 0) * nb;
    this.#abortController = new AbortController();
    if (!this.#params.showFields.includes('searchInfo')) {
      this.#params.showFields.push('searchInfo'); // needed for hitCount
    }
    return new Request(this.resource, {
      signal: this.#abortController.signal,
      method: 'POST',
      body: new URLSearchParams([
        ['xpage', 'celements_ajax'],
        ['ajax_mode', 'BlogViewJson'],
        ['ajax', 1],
        ['start', start],
        ['nb', nb],
        ...this.#toEntries(this.#params),
        ...this.#toEntries(this.#filter),
        ['sortFields', this.#sortFields.join(',')],
      ]),
    });
  }

  #toEntries(obj) {
    return Object.entries(obj ?? {})
      .map(([key, val]) => [key, Array.isArray(val) ? val.join(',') : val])
      .filter(([, val]) => !!val);
  }

}

class BlogViewerElement extends HTMLElement {

  #renderer;
  #viewer;
  #currentRenderState = {};

  constructor() {
    super();
  }

  get origin() {
    return this.getAttribute('origin') || (() => {
      const parser = document.createElement('a');
      parser.href = import.meta.url;
      return parser.origin;
    })();
  }

  get blog() {
    return this.getAttribute('blog') || undefined;
  }

  set blog(value) {
    this.setAttribute('blog', value);
  }

  get viewer() {
    return this.#viewer;
  }

  get template() {
    return this.getAttribute('template') || undefined;
  }

  get mode() {
    return this.getAttribute('mode') || 'paging';
  }

  get size() {
    return Math.max(parseInt(this.getAttribute('size')), 1) || 10;
  }

  set size(value) {
    this.setAttribute('size', Math.max(value, 1));
  }

  get page() {
    return Math.max(parseInt(this.getAttribute('page')), 1) || 1;
  }

  set page(value) {
    value = Math.max(value, 1);
    if ((this.mode !== 'paging') && value < this.page) {
      console.error(this.mode, 'doesnt support page decrease');
    } else if (this.page !== value) {
      this.setAttribute('page', value);
    }
  }

  get sortFields() {
    return (this.getAttribute('sort-fields') ?? "").split(',').filter(Boolean);
  }

  get params() {
    const json = this.getAttribute('params') || '{}';
    try {
      return safeAssign(new BlogParams(), JSON.parse(json));
    } catch (error) {
      console.warn("failed parsing params", json, error);
      return new BlogParams();
    }
  }

  set params(value) {
    const params = safeAssign(new BlogParams(), value);
    this.setAttribute('params', JSON.stringify(params));
  }

  setParams(key, value) {
    this.params = { ...this.params, [key]: value };
  }

  get filter() {
    const json = this.getAttribute('filter') || '{}';
    try {
      return safeAssign(new BlogFilter(), JSON.parse(json));
    } catch (error) {
      console.warn("failed parsing filter", json, error);
      return new CollectionFilter();
    }
  }

  set filter(value) {
    const filter = safeAssign(new BlogFilter(), value);
    this.setAttribute('filter', JSON.stringify(filter));
  }

  setFilter(key, value) {
    this.filter = { ...this.filter, [key]: value };
  }

  get count() {
    return parseInt(this.getAttribute('count'));
  }

  get hasMore() {
    return this.getAttribute('has-more') === 'true';
  }

  connectedCallback() {
    this.#init(this.page);
  }

  #init(page) {
    const hookElem = this.querySelector(`.${tagName}-hook, ul, ol`) ?? this;
    const template = document.querySelector(this.template);
    this.#renderer = new CelDataRenderer(hookElem, template);
    this.#viewer = new BlogViewer(this.origin, this.blog);
    this.#viewer.filter = this.filter;
    this.#viewer.params = this.params;
    this.#collectFields(template).forEach(f => this.#viewer.params.showFields.push(f));
    this.#viewer.sortFields = this.sortFields;
    this.#resetRenderState(page);
    if (this.mode === 'loadmore') {
      this.#initLoadmore();
    }
  }

  #initLoadmore() {
    this.#forEachLoadmoreTrigger(trigger => {
      trigger.addEventListener('click', e => !e.target.disabled && this.next());
      trigger.disabled = true;
      console.debug('registered loadmore trigger', trigger, this);
    });
  }

  #collectFields(template) {
    const fields = [...template?.content.querySelectorAll('[field]') || []]
        .map(e => e.getAttribute('field'))
        .filter(Boolean);
    return [...new Set(fields)];
  }

  next() {
    this.page++;
  }

  previous() {
    this.page--;
  }

  static get initAttributes() {
    return ['origin', 'blog', 'template', 'mode'];
  }

  static get observedAttributes() {
    return ['page', 'size', 'params', 'filter', 'sort-fields'].concat(BlogViewerElement.initAttributes);
  }

  attributeChangedCallback(name, oldValue, newValue) {
    console.trace('attributeChangedCallback', name, oldValue, newValue);
    if (this.isConnected && this.viewer && (oldValue !== newValue)) {
      if (['params', 'filter', 'sort-fields'].includes(name)) {
        const key = name.replace('-f', 'F');
        this.#viewer[key] = this[key];
        this.#resetRenderState();
      } else if (name === 'size') {
        this.#resetRenderState();
      } else if (BlogViewerElement.initAttributes.includes(name)) {
        this.#init();
      } else {
        this.render();
      }
    }
  }

  render() {
    if (this.page === this.#currentRenderState.page) {
      return this.#currentRenderState.promise;
    } else {
      const pagePromise = this.viewer?.getPage(this.page, this.size);
      const renderPromise = this.#renderResults(pagePromise);
      this.#currentRenderState = Object.freeze({
        page: this.page,
        promise: renderPromise,
        loadPromise: pagePromise,
      });
      this.dispatchEvent(new CustomEvent(`progon:${tagName}:changed`,
        { detail: this.#currentRenderState }));
      return renderPromise;
    }
  }

  #renderResults(pagePromise) {
    this.#handleCounts(pagePromise.then(p => p.counts));
    const resultsPromise = pagePromise.then(p => p.results);
    if (this.mode === 'paging') {
      return this.#renderer?.replace(resultsPromise);
    } else if (this.mode === 'loadmore') {
      return this.#renderer?.append(resultsPromise);
    } else {
      throw new Error('unknown mode: ' + this.mode);
    }
  }

  async #handleCounts(countsPromise) {
    this.#forEachLoadmoreTrigger(trigger => trigger.disabled = true);
    const counts = await countsPromise;
    this.setAttribute('count', counts?.countTotal ?? '');
    const hasMore = (counts?.countAfter ?? 1) > 0;
    this.setAttribute('has-more', hasMore);
    this.#forEachLoadmoreTrigger(trigger => trigger.disabled = !hasMore);
  }

  #forEachLoadmoreTrigger(action) {
    if (this.mode === 'loadmore') {
      const loadmoreSelector = this.getAttribute('loadmore-selector') || '.loadmore';
      this.querySelectorAll(loadmoreSelector).forEach(action);
    }
  }

  async #resetRenderState(page = 1) {
    console.debug('resetRenderState', page);
    try {
      this.viewer?.abort();
      await this.#currentRenderState.promise;
    } catch (error) {
      console.error('current render failed', error);
    }
    this.#currentRenderState = {};
    this.#renderer?.remove();
    this.setAttribute('page', page);
    return this.render();
  }

  disconnectedCallback() {
    this.#viewer = null;
    this.#renderer = null;
  }
}

if (!customElements.get(tagName)) {
  customElements.define(tagName, BlogViewerElement);
}
