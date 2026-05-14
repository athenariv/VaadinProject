import { LitElement, html, css } from 'lit';

/**
 * Quill.js rich text editor web component for Vaadin.
 * Integrates Quill loaded from CDN.
 */
class QuillEditor extends LitElement {
  static get properties() {
    return {
      value: { type: String },
      placeholder: { type: String },
      readonly: { type: Boolean }
    };
  }

  static get styles() {
    return css`
      :host {
        display: block;
        width: 100%;
      }
      #toolbar {
        border: 1px solid #ccc;
        border-bottom: none;
        border-radius: 4px 4px 0 0;
        background: #f8f8f8;
        padding: 4px;
      }
      #editor-container {
        border: 1px solid #ccc;
        border-radius: 0 0 4px 4px;
        min-height: 150px;
        background: white;
      }
      #editor-container .ql-editor {
        min-height: 120px;
        font-family: inherit;
        font-size: 14px;
      }
      button.ql-active, button:hover {
        color: var(--lumo-primary-color, #1676f3);
      }
    `;
  }

  constructor() {
    super();
    this.value = '';
    this.placeholder = 'Write something...';
    this.readonly = false;
    this._quill = null;
  }

  firstUpdated() {
    this._loadQuill();
  }

  _loadQuill() {
    // CSS is loaded inside the shadow root via the render template
    // Only load the Quill JS into document head
    const existingScript = document.getElementById('quill-script');
    if (existingScript) {
      this._initQuill();
      return;
    }

    const script = document.createElement('script');
    script.id = 'quill-script';
    script.src = 'https://cdn.jsdelivr.net/npm/quill@1.3.7/dist/quill.min.js';
    script.onload = () => this._initQuill();
    document.head.appendChild(script);
  }

  _initQuill() {
    if (typeof Quill === 'undefined') return;
    const container = this.shadowRoot.getElementById('editor-container');
    const toolbar = this.shadowRoot.getElementById('toolbar');

    this._quill = new Quill(container, {
      modules: {
        toolbar: toolbar
      },
      theme: 'snow',
      placeholder: this.placeholder,
      readOnly: this.readonly
    });

    if (this.value) {
      this._quill.clipboard.dangerouslyPasteHTML(this.value);
    }

    this._quill.on('text-change', () => {
      const html = this._quill.root.innerHTML;
      this.value = html;
      this.dispatchEvent(new CustomEvent('value-changed', {
        detail: { value: html },
        bubbles: true,
        composed: true
      }));
    });
  }

  setValue(html) {
    this.value = html;
    if (this._quill) {
      this._quill.clipboard.dangerouslyPasteHTML(html || '');
    }
  }

  getValue() {
    return this._quill ? this._quill.root.innerHTML : this.value;
  }

  render() {
    return html`
      <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/quill@1.3.7/dist/quill.snow.css">
      <div id="toolbar">
        <span class="ql-formats">
          <button class="ql-bold" title="Bold"></button>
          <button class="ql-italic" title="Italic"></button>
          <button class="ql-underline" title="Underline"></button>
          <button class="ql-strike" title="Strikethrough"></button>
        </span>
        <span class="ql-formats">
          <button class="ql-header" value="1" title="Heading 1">H1</button>
          <button class="ql-header" value="2" title="Heading 2">H2</button>
        </span>
        <span class="ql-formats">
          <button class="ql-list" value="ordered" title="Ordered list"></button>
          <button class="ql-list" value="bullet" title="Bullet list"></button>
        </span>
        <span class="ql-formats">
          <button class="ql-link" title="Link"></button>
          <button class="ql-clean" title="Clear formatting"></button>
        </span>
      </div>
      <div id="editor-container"></div>
    `;
  }
}

customElements.define('quill-editor', QuillEditor);
