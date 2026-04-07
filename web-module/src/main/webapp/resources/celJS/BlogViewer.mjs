import pick from "/file/resource/deps/lodash/pick.js";
import uniq from "/file/resource/deps/lodash/uniq.js";
import {
  CelDataViewerElement,
  Config,
} from "/file/resources/celDynJS/celData/cel-data-viewer.mjs?version=20250901";

const tagName = "blog-viewer";

export class BlogFilter {
  searchTerm;
  fromPublishDate;
}

export class BlogParams extends BlogFilter {
  xpage = "celements_ajax";
  ajax_mode = "BlogViewJson";
  ajax = 1;
  showFields = [];
  start;
  nb;
  sortFields = [];
  maxImageHeight;
  maxImageWidth;
  debug;
}

class BlogConfig extends Config {
  tagName = tagName;
  ParamsClass = BlogParams;

  processParams(params) {
    params.start = params.offset;
    params.nb = params.limit;
    params.showFields = uniq([
      "searchInfo", // needed for hitCount
      ...params.showFields,
      ...params.fields,
    ]);
    return params;
  }

  extractResults(data) {
    return data.results;
  }

  extractCount(data) {
    return data.searchInfo?.hitCount?.countTotal;
  }

  extractHasMore(data) {
    return (data.searchInfo?.hitCount?.countAfter ?? 1) > 0;
  }
}

class BlogViewerElement extends CelDataViewerElement {
  #mutationObserver;

  constructor() {
    super(new BlogConfig());
  }

  get method() {
    return super.method ?? "POST";
  }

  get path() {
    return super.path ?? this.#path;
  }

  get #path() {
    if (!this.blog) throw new Error("attribute blog missing");
    return `/${this.blog.split(".").join("/")}`;
  }

  get blog() {
    return this.getAttribute("blog") || undefined;
  }

  set blog(value) {
    this.setAttribute("blog", value);
  }

  get viewer() {
    return this.loader;
  }

  get sortFields() {
    return (this.getAttribute("sort-fields") ?? "").split(",").filter(Boolean);
  }

  get params() {
    return Object.assign(super.params, this.filter, {
      sortFields: this.sortFields,
    });
  }

  get filter() {
    const ret = new BlogFilter();
    const json = this.getAttribute("filter") || "{}";
    try {
      Object.assign(ret, JSON.parse(json));
    } catch (error) {
      console.warn("failed parsing filter", json, error);
    }
    return ret;
  }

  set filter(value) {
    const filter = new BlogFilter();
    const params = this.config.createParams(value) ?? value;
    Object.assign(filter, pick(params, Object.keys(filter)));
    this.setAttribute("filter", JSON.stringify(filter));
  }

  setFilter(key, value) {
    this.filter = { ...this.filter, [key]: value };
  }

  connectedCallback() {
    super.connectedCallback();
    this.#initBlogRenderer();
    this.#initContextMenuObserver();
  }

  #initBlogRenderer() {
    this.renderer.withPreInsert((entry, data) => {
      entry.id = "Art" + this.blog + ":" + data.articleId;
      entry.classList.add("cel_cm_blog_article");
      if (!data.isPublic) entry.classList.add("cel_nav_restricted_rights");
    });
  }

  #initContextMenuObserver() {
    this.#mutationObserver?.disconnect();
    this.#mutationObserver = new MutationObserver(
      (mutations) =>
        mutations.some(
          (m) => m.type === "childList" && m.addedNodes.length > 0,
        ) && window.initContextMenuAsync?.(),
    );
    this.#mutationObserver.observe(this.renderer.htmlElem, {
      subtree: true,
      childList: true,
    });
  }

  static get initAttributes() {
    return uniq([...super.initAttributes, "blog"]);
  }

  static get observedAttributes() {
    return uniq([...super.observedAttributes, "filter", "sort-fields"]);
  }

  attributeChangedCallback(name, oldValue, newValue) {
    super.attributeChangedCallback?.(name, oldValue, newValue);
    if (
      this.isConnected &&
      this.loader &&
      oldValue !== newValue &&
      this.constructor.initAttributes.includes(name)
    ) {
      this.#initBlogRenderer();
    }
  }

  disconnectedCallback() {
    super.disconnectedCallback();
    this.#mutationObserver?.disconnect();
  }
}

if (!customElements.get(tagName)) {
  customElements.define(tagName, BlogViewerElement);
}
