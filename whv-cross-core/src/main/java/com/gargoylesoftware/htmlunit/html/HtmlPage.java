//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.gargoylesoftware.htmlunit.html;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HTMLParser.HtmlUnitDOMBuilder;
import com.gargoylesoftware.htmlunit.html.impl.SelectableTextInput;
import com.gargoylesoftware.htmlunit.html.impl.SimpleRange;
import com.gargoylesoftware.htmlunit.javascript.AbstractJavaScriptEngine;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptEngine;
import com.gargoylesoftware.htmlunit.javascript.PostponedAction;
import com.gargoylesoftware.htmlunit.javascript.SimpleScriptable;
import com.gargoylesoftware.htmlunit.javascript.host.Window;
import com.gargoylesoftware.htmlunit.javascript.host.event.BeforeUnloadEvent;
import com.gargoylesoftware.htmlunit.javascript.host.event.Event;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLDocument;
import com.gargoylesoftware.htmlunit.util.EncodingSniffer;
import com.gargoylesoftware.htmlunit.util.UrlUtils;
import net.sourceforge.htmlunit.corejs.javascript.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.*;
import org.w3c.dom.Node;
import org.w3c.dom.ranges.Range;
import org.xml.sax.Attributes;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HtmlPage extends SgmlPage {
    private static final Log LOG = LogFactory.getLog(HtmlPage.class);
    private static final Comparator<DomElement> documentPositionComparator = new HtmlPage.DocumentPositionComparator();
    private HtmlUnitDOMBuilder builder_;
    private transient Charset originalCharset_;
    private Map<String, SortedSet<DomElement>> idMap_ = Collections.synchronizedMap(new HashMap());
    private Map<String, SortedSet<DomElement>> nameMap_ = Collections.synchronizedMap(new HashMap());
    private SortedSet<BaseFrameElement> frameElements_;
    private int parserCount_;
    private int snippetParserCount_;
    private int inlineSnippetParserCount_;
    private Collection<HtmlAttributeChangeListener> attributeListeners_;
    private final Object lock_;
    private List<PostponedAction> afterLoadActions_;
    private boolean cleaning_;
    private HtmlBase base_;
    private URL baseUrl_;
    private List<AutoCloseable> autoCloseableList_;
    private ElementFromPointHandler elementFromPointHandler_;
    private DomElement elementWithFocus_;
    private List<Range> selectionRanges_;
    private static final List<String> TABBABLE_TAGS = Arrays.asList("a", "area", "button", "input", "object", "select", "textarea");
    private static final List<String> ACCEPTABLE_TAG_NAMES = Arrays.asList("a", "area", "button", "input", "label", "legend", "textarea");

    public HtmlPage(WebResponse webResponse, WebWindow webWindow) {
        super(webResponse, webWindow);
        this.frameElements_ = new TreeSet(documentPositionComparator);
        this.lock_ = new String();
        this.afterLoadActions_ = Collections.synchronizedList(new ArrayList());
        this.selectionRanges_ = new ArrayList(3);
    }

    public HtmlPage getPage() {
        return this;
    }

    public boolean hasCaseSensitiveTagNames() {
        return false;
    }

    public void initialize() throws IOException, FailingHttpStatusCodeException {
        WebWindow enclosingWindow = this.getEnclosingWindow();
        boolean isAboutBlank = this.getUrl() == WebClient.URL_ABOUT_BLANK;
        if (isAboutBlank) {
            if (enclosingWindow instanceof FrameWindow && !((FrameWindow) enclosingWindow).getFrameElement().isContentLoaded()) {
                return;
            }

            if (enclosingWindow instanceof TopLevelWindow) {
                TopLevelWindow topWindow = (TopLevelWindow) enclosingWindow;
                WebWindow openerWindow = topWindow.getOpener();
                if (openerWindow != null && openerWindow.getEnclosedPage() != null) {
                    this.baseUrl_ = openerWindow.getEnclosedPage().getWebResponse().getWebRequest().getUrl();
                }
            }
        }

        this.loadFrames();
        if (!isAboutBlank) {
            if (this.hasFeature(BrowserVersionFeatures.FOCUS_BODY_ELEMENT_AT_START)) {
                this.setElementWithFocus(this.getBody());
            }

            this.setReadyState("complete");
            this.getDocumentElement().setReadyState("complete");
        }

        this.executeEventHandlersIfNeeded("DOMContentLoaded");
        this.executeDeferredScriptsIfNeeded();
        this.setReadyStateOnDeferredScriptsIfNeeded();
        boolean isFrameWindow = enclosingWindow instanceof FrameWindow;
        boolean isFirstPageInFrameWindow = false;
        if (isFrameWindow) {
            isFrameWindow = ((FrameWindow) enclosingWindow).getFrameElement() instanceof HtmlFrame;
            History hist = enclosingWindow.getHistory();
            if (hist.getLength() > 0 && WebClient.URL_ABOUT_BLANK == hist.getUrl(0)) {
                isFirstPageInFrameWindow = hist.getLength() <= 2;
            } else {
                isFirstPageInFrameWindow = enclosingWindow.getHistory().getLength() < 2;
            }
        }

        if (isFrameWindow && !isFirstPageInFrameWindow) {
            this.executeEventHandlersIfNeeded("load");
        }

        Iterator var12 = this.getFrames().iterator();

        while (var12.hasNext()) {
            FrameWindow frameWindow = (FrameWindow) var12.next();
            if (frameWindow.getFrameElement() instanceof HtmlFrame) {
                Page page = frameWindow.getEnclosedPage();
                if (page != null && page.isHtmlPage()) {
                    ((HtmlPage) page).executeEventHandlersIfNeeded("load");
                }
            }
        }

        if (!isFrameWindow) {
            this.executeEventHandlersIfNeeded("load");
        }

        try {
            while (!this.afterLoadActions_.isEmpty()) {
                PostponedAction action = (PostponedAction) this.afterLoadActions_.remove(0);
                action.execute();
            }
        } catch (IOException var8) {
            throw var8;
        } catch (Exception var9) {
            throw new RuntimeException(var9);
        }

        this.executeRefreshIfNeeded();
    }

    void addAfterLoadAction(PostponedAction action) {
        this.afterLoadActions_.add(action);
    }

    public void cleanUp() {
        if (!this.cleaning_) {
            this.cleaning_ = true;
            super.cleanUp();
            this.executeEventHandlersIfNeeded("unload");
            this.deregisterFramesIfNeeded();
            this.cleaning_ = false;
            if (this.autoCloseableList_ != null) {
                Iterator var1 = (new ArrayList(this.autoCloseableList_)).iterator();

                while (var1.hasNext()) {
                    AutoCloseable closeable = (AutoCloseable) var1.next();

                    try {
                        closeable.close();
                    } catch (Exception var4) {
                        throw new RuntimeException(var4);
                    }
                }
            }

        }
    }

    public HtmlElement getDocumentElement() {
        return (HtmlElement) super.getDocumentElement();
    }

    public HtmlElement getBody() {
        DomElement doc = this.getDocumentElement();
        if (doc != null) {
            Iterator var2 = doc.getChildren().iterator();

            while (var2.hasNext()) {
                DomNode node = (DomNode) var2.next();
                if (node instanceof HtmlBody || node instanceof HtmlFrameSet) {
                    return (HtmlElement) node;
                }
            }
        }

        return null;
    }

    public HtmlElement getHead() {
        DomElement doc = this.getDocumentElement();
        if (doc != null) {
            Iterator var2 = doc.getChildren().iterator();

            while (var2.hasNext()) {
                DomNode node = (DomNode) var2.next();
                if (node instanceof HtmlHead) {
                    return (HtmlElement) node;
                }
            }
        }

        return null;
    }

    public Document getOwnerDocument() {
        return null;
    }

    public Node importNode(Node importedNode, boolean deep) {
        throw new UnsupportedOperationException("HtmlPage.importNode is not yet implemented.");
    }

    public String getInputEncoding() {
        throw new UnsupportedOperationException("HtmlPage.getInputEncoding is not yet implemented.");
    }

    public String getXmlEncoding() {
        return null;
    }

    public boolean getXmlStandalone() {
        return false;
    }

    public void setXmlStandalone(boolean xmlStandalone) throws DOMException {
        throw new UnsupportedOperationException("HtmlPage.setXmlStandalone is not yet implemented.");
    }

    public String getXmlVersion() {
        return null;
    }

    public void setXmlVersion(String xmlVersion) throws DOMException {
        throw new UnsupportedOperationException("HtmlPage.setXmlVersion is not yet implemented.");
    }

    public boolean getStrictErrorChecking() {
        throw new UnsupportedOperationException("HtmlPage.getStrictErrorChecking is not yet implemented.");
    }

    public void setStrictErrorChecking(boolean strictErrorChecking) {
        throw new UnsupportedOperationException("HtmlPage.setStrictErrorChecking is not yet implemented.");
    }

    public String getDocumentURI() {
        throw new UnsupportedOperationException("HtmlPage.getDocumentURI is not yet implemented.");
    }

    public void setDocumentURI(String documentURI) {
        throw new UnsupportedOperationException("HtmlPage.setDocumentURI is not yet implemented.");
    }

    public Node adoptNode(Node source) throws DOMException {
        throw new UnsupportedOperationException("HtmlPage.adoptNode is not yet implemented.");
    }

    public DOMConfiguration getDomConfig() {
        throw new UnsupportedOperationException("HtmlPage.getDomConfig is not yet implemented.");
    }

    public Node renameNode(Node newNode, String namespaceURI, String qualifiedName) throws DOMException {
        throw new UnsupportedOperationException("HtmlPage.renameNode is not yet implemented.");
    }

    public Charset getCharset() {
        if (this.originalCharset_ == null) {
            this.originalCharset_ = this.getWebResponse().getContentCharset();
        }

        return this.originalCharset_;
    }

    public String getContentType() {
        return this.getWebResponse().getContentType();
    }

    public DOMImplementation getImplementation() {
        throw new UnsupportedOperationException("HtmlPage.getImplementation is not yet implemented.");
    }

    public DomElement createElement(String tagName) {
        if (tagName.indexOf(58) == -1) {
            tagName = tagName.toLowerCase(Locale.ROOT);
        }

        return HTMLParser.getFactory(tagName).createElementNS(this, (String) null, tagName, (Attributes) null, true);
    }

    public DomElement createElementNS(String namespaceURI, String qualifiedName) {
        return HTMLParser.getElementFactory(this, namespaceURI, qualifiedName, false, true).createElementNS(this, namespaceURI, qualifiedName, (Attributes) null, true);
    }

    public Attr createAttributeNS(String namespaceURI, String qualifiedName) {
        throw new UnsupportedOperationException("HtmlPage.createAttributeNS is not yet implemented.");
    }

    public EntityReference createEntityReference(String id) {
        throw new UnsupportedOperationException("HtmlPage.createEntityReference is not yet implemented.");
    }

    public ProcessingInstruction createProcessingInstruction(String namespaceURI, String qualifiedName) {
        throw new UnsupportedOperationException("HtmlPage.createProcessingInstruction is not yet implemented.");
    }

    public DomElement getElementById(String elementId) {
        SortedSet<DomElement> elements = (SortedSet) this.idMap_.get(elementId);
        return elements != null ? (DomElement) elements.first() : null;
    }

    public HtmlAnchor getAnchorByName(String name) throws ElementNotFoundException {
        return (HtmlAnchor) this.getDocumentElement().getOneHtmlElementByAttribute("a", "name", name);
    }

    public HtmlAnchor getAnchorByHref(String href) throws ElementNotFoundException {
        return (HtmlAnchor) this.getDocumentElement().getOneHtmlElementByAttribute("a", "href", href);
    }

    public List<HtmlAnchor> getAnchors() {
        return this.getDocumentElement().getElementsByTagNameImpl("a");
    }

    public HtmlAnchor getAnchorByText(String text) throws ElementNotFoundException {
        WebAssert.notNull("text", text);
        Iterator var2 = this.getAnchors().iterator();

        HtmlAnchor anchor;
        do {
            if (!var2.hasNext()) {
                throw new ElementNotFoundException("a", "<text>", text);
            }

            anchor = (HtmlAnchor) var2.next();
        } while (!text.equals(anchor.asText()));

        return anchor;
    }

    public HtmlForm getFormByName(String name) throws ElementNotFoundException {
        List<HtmlForm> forms = this.getDocumentElement().getElementsByAttribute("form", "name", name);
        if (forms.isEmpty()) {
            throw new ElementNotFoundException("form", "name", name);
        } else {
            return (HtmlForm) forms.get(0);
        }
    }

    public List<HtmlForm> getForms() {
        return this.getDocumentElement().getElementsByTagNameImpl("form");
    }

    public URL getFullyQualifiedUrl(String relativeUrl) throws MalformedURLException {
        if (this.hasFeature(BrowserVersionFeatures.URL_MISSING_SLASHES)) {
            for (boolean incorrectnessNotified = false; relativeUrl.startsWith("http:") && !relativeUrl.startsWith("http://"); relativeUrl = "http:/" + relativeUrl.substring(5)) {
                if (!incorrectnessNotified) {
                    this.notifyIncorrectness("Incorrect URL \"" + relativeUrl + "\" has been corrected");
                    incorrectnessNotified = true;
                }
            }
        }

        return WebClient.expandUrl(this.getBaseURL(), relativeUrl);
    }

    public String getResolvedTarget(String elementTarget) {
        String resolvedTarget;
        if (this.base_ == null) {
            resolvedTarget = elementTarget;
        } else if (elementTarget != null && !elementTarget.isEmpty()) {
            resolvedTarget = elementTarget;
        } else {
            resolvedTarget = this.base_.getTargetAttribute();
        }

        return resolvedTarget;
    }

    public List<String> getTabbableElementIds() {
        List<String> list = new ArrayList();
        Iterator var2 = this.getTabbableElements().iterator();

        while (var2.hasNext()) {
            HtmlElement element = (HtmlElement) var2.next();
            list.add(element.getId());
        }

        return Collections.unmodifiableList(list);
    }

    public List<HtmlElement> getTabbableElements() {
        List<HtmlElement> tabbableElements = new ArrayList();
        Iterator var2 = this.getHtmlElementDescendants().iterator();

        while (var2.hasNext()) {
            HtmlElement element = (HtmlElement) var2.next();
            String tagName = element.getTagName();
            if (TABBABLE_TAGS.contains(tagName)) {
                boolean disabled = element.hasAttribute("disabled");
                if (!disabled && element.getTabIndex() != HtmlElement.TAB_INDEX_OUT_OF_BOUNDS) {
                    tabbableElements.add(element);
                }
            }
        }

        Collections.sort(tabbableElements, createTabOrderComparator());
        return Collections.unmodifiableList(tabbableElements);
    }

    private static Comparator<HtmlElement> createTabOrderComparator() {
        return new Comparator<HtmlElement>() {
            public int compare(HtmlElement element1, HtmlElement element2) {
                Short i1 = element1.getTabIndex();
                Short i2 = element2.getTabIndex();
                short index1;
                if (i1 != null) {
                    index1 = i1;
                } else {
                    index1 = -1;
                }

                short index2;
                if (i2 != null) {
                    index2 = i2;
                } else {
                    index2 = -1;
                }

                int result;
                if (index1 > 0 && index2 > 0) {
                    result = index1 - index2;
                } else if (index1 > 0) {
                    result = -1;
                } else if (index2 > 0) {
                    result = 1;
                } else if (index1 == index2) {
                    result = 0;
                } else {
                    result = index2 - index1;
                }

                return result;
            }
        };
    }

    public HtmlElement getHtmlElementByAccessKey(char accessKey) {
        List<HtmlElement> elements = this.getHtmlElementsByAccessKey(accessKey);
        return elements.isEmpty() ? null : (HtmlElement) elements.get(0);
    }

    public List<HtmlElement> getHtmlElementsByAccessKey(char accessKey) {
        List<HtmlElement> elements = new ArrayList();
        String searchString = Character.toString(accessKey).toLowerCase(Locale.ROOT);
        Iterator var4 = this.getHtmlElementDescendants().iterator();

        while (var4.hasNext()) {
            HtmlElement element = (HtmlElement) var4.next();
            if (ACCEPTABLE_TAG_NAMES.contains(element.getTagName())) {
                String accessKeyAttribute = element.getAttributeDirect("accesskey");
                if (searchString.equalsIgnoreCase(accessKeyAttribute)) {
                    elements.add(element);
                }
            }
        }

        return elements;
    }

    public ScriptResult executeJavaScript(String sourceCode) {
        return this.executeJavaScript(sourceCode, "injected script", 1);
    }

    public ScriptResult executeJavaScript(String sourceCode, String sourceName, int startLine) {
        if (!this.getWebClient().getOptions().isJavaScriptEnabled()) {
            return new ScriptResult((Object) null, this);
        } else {
            if (StringUtils.startsWithIgnoreCase(sourceCode, "javascript:")) {
                sourceCode = sourceCode.substring("javascript:".length()).trim();
                if (sourceCode.startsWith("return ")) {
                    sourceCode = sourceCode.substring("return ".length());
                }
            }

            Object result = this.getWebClient().getJavaScriptEngine().execute(this, sourceCode, sourceName, startLine);
            return new ScriptResult(result, this.getWebClient().getCurrentWindow().getEnclosedPage());
        }
    }

    HtmlPage.JavaScriptLoadResult loadExternalJavaScriptFile(String srcAttribute, Charset scriptCharset) throws FailingHttpStatusCodeException {
        WebClient client = this.getWebClient();
        if (!StringUtils.isBlank(srcAttribute) && client.getOptions().isJavaScriptEnabled()) {
            URL scriptURL;
            try {
                scriptURL = this.getFullyQualifiedUrl(srcAttribute);
                String protocol = scriptURL.getProtocol();
                if ("javascript".equals(protocol) || scriptURL.getHost().contains("google")) {
                    LOG.info("Ignoring script src [" + srcAttribute + "]");
                    return HtmlPage.JavaScriptLoadResult.NOOP;
                }

                if (!"http".equals(protocol) && !"https".equals(protocol) && !"data".equals(protocol) && !"file".equals(protocol)) {
                    client.getJavaScriptErrorListener().malformedScriptURL(this, srcAttribute, new MalformedURLException("unknown protocol: '" + protocol + "'"));
                    return HtmlPage.JavaScriptLoadResult.NOOP;
                }
            } catch (MalformedURLException var9) {
                client.getJavaScriptErrorListener().malformedScriptURL(this, srcAttribute, var9);
                return HtmlPage.JavaScriptLoadResult.NOOP;
            }

            Object script;
            try {
                script = this.loadJavaScriptFromUrl(scriptURL, scriptCharset);
            } catch (IOException var7) {
                client.getJavaScriptErrorListener().loadScriptError(this, scriptURL, var7);
                return HtmlPage.JavaScriptLoadResult.DOWNLOAD_ERROR;
            } catch (FailingHttpStatusCodeException var8) {
                client.getJavaScriptErrorListener().loadScriptError(this, scriptURL, var8);
                throw var8;
            }

            if (script == null) {
                return HtmlPage.JavaScriptLoadResult.COMPILATION_ERROR;
            } else {
                AbstractJavaScriptEngine<Object> engine = (AbstractJavaScriptEngine<Object>) client.getJavaScriptEngine();
                engine.execute(this, script);
                return HtmlPage.JavaScriptLoadResult.SUCCESS;
            }
        } else {
            return HtmlPage.JavaScriptLoadResult.NOOP;
        }
    }

    private Object loadJavaScriptFromUrl(URL url, Charset scriptCharset) throws IOException, FailingHttpStatusCodeException {
        WebRequest referringRequest = this.getWebResponse().getWebRequest();
        WebClient client = this.getWebClient();
        BrowserVersion browser = client.getBrowserVersion();
        WebRequest request = new WebRequest(url, browser.getScriptAcceptHeader());
        request.setAdditionalHeaders(new HashMap(referringRequest.getAdditionalHeaders()));
        request.setAdditionalHeader("Referer", referringRequest.getUrl().toString());
        request.setAdditionalHeader("Accept", client.getBrowserVersion().getScriptAcceptHeader());
        WebResponse response = client.loadWebResponse(request);
        Cache cache = client.getCache();
        Object cachedScript = cache.getCachedObject(request);
        if (cachedScript instanceof Script) {
            return cachedScript;
        } else {
            client.printContentIfNecessary(response);
            client.throwFailingHttpStatusCodeExceptionIfNecessary(response);
            int statusCode = response.getStatusCode();
            boolean successful = statusCode >= 200 && statusCode < 300;
            boolean noContent = statusCode == 204;
            if (successful && !noContent) {
                String contentType = response.getContentType();
                if (!"application/javascript".equalsIgnoreCase(contentType) && !"application/ecmascript".equalsIgnoreCase(contentType)) {
                    if (!"text/javascript".equals(contentType) && !"text/ecmascript".equals(contentType) && !"application/x-javascript".equalsIgnoreCase(contentType)) {
                        this.getWebClient().getIncorrectnessListener().notify("Expected content type of 'application/javascript' or 'application/ecmascript' for remotely loaded JavaScript element at '" + url + "', but got '" + contentType + "'.", this);
                    } else {
                        this.getWebClient().getIncorrectnessListener().notify("Obsolete content type encountered: '" + contentType + "'.", this);
                    }
                }

                Charset scriptEncoding = Charset.forName("windows-1252");
                boolean ignoreBom = false;
                Charset contentCharset = EncodingSniffer.sniffEncodingFromHttpHeaders(response.getResponseHeaders());
                if (contentCharset == null) {
                    if (scriptCharset != null && StandardCharsets.ISO_8859_1 != scriptCharset) {
                        ignoreBom = true;
                        scriptEncoding = scriptCharset;
                    } else {
                        ignoreBom = StandardCharsets.ISO_8859_1 != scriptCharset;
                    }
                } else if (StandardCharsets.ISO_8859_1 != contentCharset) {
                    ignoreBom = true;
                    scriptEncoding = contentCharset;
                } else {
                    ignoreBom = true;
                }

                String scriptCode = response.getContentAsString(scriptEncoding, ignoreBom && this.getWebClient().getBrowserVersion().hasFeature(BrowserVersionFeatures.JS_IGNORES_UTF8_BOM_SOMETIMES));
                if (null != scriptCode) {
                    AbstractJavaScriptEngine<?> javaScriptEngine = client.getJavaScriptEngine();
                    Object script = javaScriptEngine.compile(this, scriptCode, url.toExternalForm(), 1);
                    if (script != null && cache.cacheIfPossible(request, response, script)) {
                        return script;
                    } else {
                        response.cleanUp();
                        return script;
                    }
                } else {
                    response.cleanUp();
                    return null;
                }
            } else {
                throw new IOException("Unable to download JavaScript from '" + url + "' (status " + statusCode + ").");
            }
        }
    }

    public String getTitleText() {
        HtmlTitle titleElement = this.getTitleElement();
        return titleElement != null ? titleElement.asText() : "";
    }

    public void setTitleText(String message) {
        HtmlTitle titleElement = this.getTitleElement();
        if (titleElement == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No title element, creating one");
            }

            HtmlHead head = (HtmlHead) getFirstChildElement(this.getDocumentElement(), HtmlHead.class);
            if (head == null) {
                throw new IllegalStateException("Headelement was not defined for this page");
            }

            Map<String, DomAttr> emptyMap = Collections.emptyMap();
            titleElement = new HtmlTitle("title", this, emptyMap);
            if (head.getFirstChild() != null) {
                head.getFirstChild().insertBefore(titleElement);
            } else {
                head.appendChild(titleElement);
            }
        }

        titleElement.setNodeValue(message);
    }

    private static DomElement getFirstChildElement(DomElement startElement, Class<?> clazz) {
        if (startElement == null) {
            return null;
        } else {
            Iterator var2 = startElement.getChildElements().iterator();

            DomElement element;
            do {
                if (!var2.hasNext()) {
                    return null;
                }

                element = (DomElement) var2.next();
            } while (!clazz.isInstance(element));

            return element;
        }
    }

    private DomElement getFirstChildElementRecursive(DomElement startElement, Class<?> clazz) {
        if (startElement == null) {
            return null;
        } else {
            Iterator var3 = startElement.getChildElements().iterator();

            DomElement childFound;
            do {
                if (!var3.hasNext()) {
                    return null;
                }

                DomElement element = (DomElement) var3.next();
                if (clazz.isInstance(element)) {
                    return element;
                }

                childFound = this.getFirstChildElementRecursive(element, clazz);
            } while (childFound == null);

            return childFound;
        }
    }

    private HtmlTitle getTitleElement() {
        return (HtmlTitle) this.getFirstChildElementRecursive(this.getDocumentElement(), HtmlTitle.class);
    }

    private boolean executeEventHandlersIfNeeded(String eventType) {
        if (!this.getWebClient().getOptions().isJavaScriptEnabled()) {
            return true;
        } else {
            WebWindow window = this.getEnclosingWindow();
            if (window.getScriptableObject() instanceof Window) {
                DomElement element = this.getDocumentElement();
                if (element == null) {
                    return true;
                }

                Object event;
                if (eventType.equals("beforeunload")) {
                    event = new BeforeUnloadEvent(element, eventType);
                } else {
                    event = new Event(element, eventType);
                }

                ScriptResult result = element.fireEvent((Event) event);
                if (!this.isOnbeforeunloadAccepted(this, (Event) event, result)) {
                    return false;
                }
            }

            if (window instanceof FrameWindow) {
                FrameWindow fw = (FrameWindow) window;
                BaseFrameElement frame = fw.getFrameElement();
                if ("load".equals(eventType) && frame.getParentNode() instanceof DomDocumentFragment) {
                    return true;
                }

                if (frame.hasEventHandlers("on" + eventType)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Executing on" + eventType + " handler for " + frame);
                    }

                    if (window.getScriptableObject() instanceof Window) {
                        Object event;
                        if (eventType.equals("beforeunload")) {
                            event = new BeforeUnloadEvent(frame, eventType);
                        } else {
                            event = new Event(frame, eventType);
                        }

                        ScriptResult result = ((com.gargoylesoftware.htmlunit.javascript.host.dom.Node) frame.getScriptableObject()).executeEventLocally((Event) event);
                        if (!this.isOnbeforeunloadAccepted((HtmlPage) frame.getPage(), (Event) event, result)) {
                            return false;
                        }
                    }
                }
            }

            return true;
        }
    }

    public boolean isOnbeforeunloadAccepted() {
        return this.executeEventHandlersIfNeeded("beforeunload");
    }

    private boolean isOnbeforeunloadAccepted(HtmlPage page, Event event, ScriptResult result) {
        if (event.getType().equals("beforeunload")) {
            boolean ie = this.hasFeature(BrowserVersionFeatures.JS_CALL_RESULT_IS_LAST_RETURN_VALUE);
            String message = getBeforeUnloadMessage(event, result, ie);
            if (message != null) {
                OnbeforeunloadHandler handler = this.getWebClient().getOnbeforeunloadHandler();
                if (handler != null) {
                    return handler.handleEvent(page, message);
                }

                LOG.warn("document.onbeforeunload() returned a string in event.returnValue, but no onbeforeunload handler installed.");
            }
        }

        return true;
    }

    private static String getBeforeUnloadMessage(Event event, ScriptResult result, boolean ie) {
        String message = null;
        if (event.getReturnValue() != Undefined.instance) {
            if (!ie || event.getReturnValue() != null || result == null || result.getJavaScriptResult() == null || result.getJavaScriptResult() == Undefined.instance) {
                message = Context.toString(event.getReturnValue());
            }
        } else if (result != null) {
            if (ie) {
                if (result.getJavaScriptResult() != Undefined.instance) {
                    message = Context.toString(result.getJavaScriptResult());
                }
            } else if (result.getJavaScriptResult() != null && result.getJavaScriptResult() != Undefined.instance) {
                message = Context.toString(result.getJavaScriptResult());
            }
        }

        return message;
    }

    private void executeRefreshIfNeeded() throws IOException {
        WebWindow window = this.getEnclosingWindow();
        if (window != null) {
            String refreshString = this.getRefreshStringOrNull();
            if (refreshString != null && !refreshString.isEmpty()) {
                int index = StringUtils.indexOfAnyBut(refreshString, "0123456789");
                boolean timeOnly = index == -1;
                double time;
                URL url;
                if (timeOnly) {
                    try {
                        time = Double.parseDouble(refreshString);
                    } catch (NumberFormatException var13) {
                        LOG.error("Malformed refresh string (no ';' but not a number): " + refreshString, var13);
                        return;
                    }

                    url = this.getUrl();
                } else {
                    try {
                        time = Double.parseDouble(refreshString.substring(0, index).trim());
                    } catch (NumberFormatException var12) {
                        LOG.error("Malformed refresh string (no valid number before ';') " + refreshString, var12);
                        return;
                    }

                    index = refreshString.toLowerCase(Locale.ROOT).indexOf("url=", index);
                    if (index == -1) {
                        LOG.error("Malformed refresh string (found ';' but no 'url='): " + refreshString);
                        return;
                    }

                    StringBuilder builder = new StringBuilder(refreshString.substring(index + 4));
                    if (StringUtils.isBlank(builder.toString())) {
                        url = this.getUrl();
                    } else {
                        if (builder.charAt(0) == '"' || builder.charAt(0) == '\'') {
                            builder.deleteCharAt(0);
                        }

                        if (builder.charAt(builder.length() - 1) == '"' || builder.charAt(builder.length() - 1) == '\'') {
                            builder.deleteCharAt(builder.length() - 1);
                        }

                        String urlString = builder.toString();

                        try {
                            url = this.getFullyQualifiedUrl(urlString);
                        } catch (MalformedURLException var11) {
                            LOG.error("Malformed URL in refresh string: " + refreshString, var11);
                            throw var11;
                        }
                    }
                }

                int timeRounded = (int) time;
                this.checkRecursion();
                this.getWebClient().getRefreshHandler().handleRefresh(this, url, timeRounded);
            }
        }
    }

    private void checkRecursion() {
        StackTraceElement[] elements = (new Exception()).getStackTrace();
        if (elements.length > 500) {
            for (int i = 0; i < 500; ++i) {
                if (!elements[i].getClassName().startsWith("com.gargoylesoftware.htmlunit.")) {
                    return;
                }
            }

            WebResponse webResponse = this.getWebResponse();
            throw new FailingHttpStatusCodeException("Too much redirect for " + webResponse.getWebRequest().getUrl(), webResponse);
        }
    }

    private String getRefreshStringOrNull() {
        List<HtmlMeta> metaTags = this.getMetaTags("refresh");
        return !metaTags.isEmpty() ? ((HtmlMeta) metaTags.get(0)).getContentAttribute().trim() : this.getWebResponse().getResponseHeaderValue("Refresh");
    }

    private void executeDeferredScriptsIfNeeded() {
        if (this.getWebClient().getOptions().isJavaScriptEnabled()) {
            if (this.hasFeature(BrowserVersionFeatures.JS_DEFERRED)) {
                DomElement doc = this.getDocumentElement();
                List<HtmlElement> elements = doc.getElementsByTagName("script");
                Iterator var3 = elements.iterator();

                while (var3.hasNext()) {
                    HtmlElement e = (HtmlElement) var3.next();
                    if (e instanceof HtmlScript) {
                        HtmlScript script = (HtmlScript) e;
                        if (script.isDeferred()) {
                            script.executeScriptIfNeeded();
                        }
                    }
                }
            }

        }
    }

    private void setReadyStateOnDeferredScriptsIfNeeded() {
        if (this.getWebClient().getOptions().isJavaScriptEnabled() && this.hasFeature(BrowserVersionFeatures.JS_DEFERRED)) {
            List<HtmlElement> elements = this.getDocumentElement().getElementsByTagName("script");
            Iterator var2 = elements.iterator();

            while (var2.hasNext()) {
                HtmlElement e = (HtmlElement) var2.next();
                if (e instanceof HtmlScript) {
                    HtmlScript script = (HtmlScript) e;
                    if (script.isDeferred()) {
                        script.setAndExecuteReadyState("complete");
                    }
                }
            }
        }

    }

    public void deregisterFramesIfNeeded() {
        Iterator var1 = this.getFrames().iterator();

        while (var1.hasNext()) {
            WebWindow window = (WebWindow) var1.next();
            this.getWebClient().deregisterWebWindow(window);
            Page page = window.getEnclosedPage();
            if (page != null && page.isHtmlPage()) {
                ((HtmlPage) page).deregisterFramesIfNeeded();
            }
        }

    }

    public List<FrameWindow> getFrames() {
        List<FrameWindow> list = new ArrayList(this.frameElements_.size());
        Iterator var2 = this.frameElements_.iterator();

        while (var2.hasNext()) {
            BaseFrameElement frameElement = (BaseFrameElement) var2.next();
            list.add(frameElement.getEnclosedWindow());
        }

        return list;
    }

    public FrameWindow getFrameByName(String name) throws ElementNotFoundException {
        Iterator var2 = this.getFrames().iterator();

        FrameWindow frame;
        do {
            if (!var2.hasNext()) {
                throw new ElementNotFoundException("frame or iframe", "name", name);
            }

            frame = (FrameWindow) var2.next();
        } while (!frame.getName().equals(name));

        return frame;
    }

    public DomElement pressAccessKey(char accessKey) throws IOException {
        HtmlElement element = this.getHtmlElementByAccessKey(accessKey);
        if (element != null) {
            element.focus();
            if (element instanceof HtmlAnchor || element instanceof HtmlArea || element instanceof HtmlButton || element instanceof HtmlInput || element instanceof HtmlLabel || element instanceof HtmlLegend || element instanceof HtmlTextArea) {
                Page newPage = element.click();
                if (newPage != this && this.getFocusedElement() == element) {
                    this.getFocusedElement().blur();
                }
            }
        }

        return this.getFocusedElement();
    }

    public HtmlElement tabToNextElement() {
        List<HtmlElement> elements = this.getTabbableElements();
        if (elements.isEmpty()) {
            this.setFocusedElement((DomElement) null);
            return null;
        } else {
            DomElement elementWithFocus = this.getFocusedElement();
            HtmlElement elementToGiveFocus;
            if (elementWithFocus == null) {
                elementToGiveFocus = (HtmlElement) elements.get(0);
            } else {
                int index = elements.indexOf(elementWithFocus);
                if (index == -1) {
                    elementToGiveFocus = (HtmlElement) elements.get(0);
                } else if (index == elements.size() - 1) {
                    elementToGiveFocus = (HtmlElement) elements.get(0);
                } else {
                    elementToGiveFocus = (HtmlElement) elements.get(index + 1);
                }
            }

            this.setFocusedElement(elementToGiveFocus);
            return elementToGiveFocus;
        }
    }

    public HtmlElement tabToPreviousElement() {
        List<HtmlElement> elements = this.getTabbableElements();
        if (elements.isEmpty()) {
            this.setFocusedElement((DomElement) null);
            return null;
        } else {
            DomElement elementWithFocus = this.getFocusedElement();
            HtmlElement elementToGiveFocus;
            if (elementWithFocus == null) {
                elementToGiveFocus = (HtmlElement) elements.get(elements.size() - 1);
            } else {
                int index = elements.indexOf(elementWithFocus);
                if (index == -1) {
                    elementToGiveFocus = (HtmlElement) elements.get(elements.size() - 1);
                } else if (index == 0) {
                    elementToGiveFocus = (HtmlElement) elements.get(elements.size() - 1);
                } else {
                    elementToGiveFocus = (HtmlElement) elements.get(index - 1);
                }
            }

            this.setFocusedElement(elementToGiveFocus);
            return elementToGiveFocus;
        }
    }

    public <E extends HtmlElement> E getHtmlElementById(String elementId) throws ElementNotFoundException {
        DomElement element = this.getElementById(elementId);
        if (element == null) {
            throw new ElementNotFoundException("*", "id", elementId);
        } else {
            return (E) element;
        }
    }

    public List<DomElement> getElementsById(String elementId) {
        SortedSet<DomElement> elements = (SortedSet) this.idMap_.get(elementId);
        return (List) (elements != null ? new ArrayList(elements) : Collections.emptyList());
    }

    public <E extends DomElement> E getElementByName(String name) throws ElementNotFoundException {
        SortedSet<DomElement> elements = (SortedSet) this.nameMap_.get(name);
        if (elements != null) {
            return (E) elements.first();
        } else {
            throw new ElementNotFoundException("*", "name", name);
        }
    }

    public List<DomElement> getElementsByName(String name) {
        SortedSet<DomElement> elements = (SortedSet) this.nameMap_.get(name);
        return (List) (elements != null ? new ArrayList(elements) : Collections.emptyList());
    }

    public List<DomElement> getElementsByIdAndOrName(String idAndOrName) {
        Collection<DomElement> list1 = (Collection) this.idMap_.get(idAndOrName);
        Collection<DomElement> list2 = (Collection) this.nameMap_.get(idAndOrName);
        List<DomElement> list = new ArrayList();
        if (list1 != null) {
            list.addAll(list1);
        }

        if (list2 != null) {
            Iterator var5 = list2.iterator();

            while (var5.hasNext()) {
                DomElement elt = (DomElement) var5.next();
                if (!list.contains(elt)) {
                    list.add(elt);
                }
            }
        }

        return list;
    }

    void notifyNodeAdded(DomNode node) {
        if (node instanceof DomElement) {
            this.addMappedElement((DomElement) node, true);
            if (node instanceof BaseFrameElement) {
                this.frameElements_.add((BaseFrameElement) node);
            }

            Iterator var2 = node.getHtmlElementDescendants().iterator();

            while (var2.hasNext()) {
                HtmlElement child = (HtmlElement) var2.next();
                if (child instanceof BaseFrameElement) {
                    this.frameElements_.add((BaseFrameElement) child);
                }
            }

            if ("base".equals(node.getNodeName())) {
                this.calculateBase();
            }
        }

        node.onAddedToPage();
    }

    void notifyNodeRemoved(DomNode node) {
        if (node instanceof HtmlElement) {
            this.removeMappedElement((HtmlElement) node, true, true);
            if (node instanceof BaseFrameElement) {
                this.frameElements_.remove(node);
            }

            Iterator var2 = node.getHtmlElementDescendants().iterator();

            while (var2.hasNext()) {
                HtmlElement child = (HtmlElement) var2.next();
                if (child instanceof BaseFrameElement) {
                    this.frameElements_.remove(child);
                }
            }

            if ("base".equals(node.getNodeName())) {
                this.calculateBase();
            }
        }

    }

    void addMappedElement(DomElement element) {
        this.addMappedElement(element, false);
    }

    void addMappedElement(DomElement element, boolean recurse) {
        if (this.isAncestorOf(element)) {
            this.addElement(this.idMap_, element, "id", recurse);
            this.addElement(this.nameMap_, element, "name", recurse);
        }

    }

    private void addElement(Map<String, SortedSet<DomElement>> map, DomElement element, String attribute, boolean recurse) {
        String value = getAttributeValue(element, attribute);
        if (DomElement.ATTRIBUTE_NOT_DEFINED != value) {
            SortedSet<DomElement> elements = map.get(value);
            if (elements == null) {
                elements = new TreeSet<>(documentPositionComparator);
                elements.add(element);
                map.put(value, elements);
            } else if (!elements.contains(element)) {
                elements.add(element);
            }
        }

        if (recurse) {
            Iterator var9 = element.getChildElements().iterator();

            while (var9.hasNext()) {
                DomElement child = (DomElement) var9.next();
                this.addElement(map, child, attribute, true);
            }
        }

    }

    private static String getAttributeValue(DomElement element, String attribute) {
        String value = element.getAttribute(attribute);
        if (DomElement.ATTRIBUTE_NOT_DEFINED == value && !(element instanceof HtmlApplet) && !(element instanceof HtmlObject)) {
            Object o = element.getScriptableObject();
            if (o instanceof ScriptableObject) {
                ScriptableObject scriptObject = (ScriptableObject) o;
                if (scriptObject.has(attribute, scriptObject)) {
                    Object jsValue = scriptObject.get(attribute, scriptObject);
                    if (jsValue != Scriptable.NOT_FOUND && jsValue instanceof String) {
                        value = (String) jsValue;
                    }
                }
            }
        }

        return value;
    }

    void removeMappedElement(HtmlElement element) {
        this.removeMappedElement(element, false, false);
    }

    void removeMappedElement(DomElement element, boolean recurse, boolean descendant) {
        if (descendant || this.isAncestorOf(element)) {
            this.removeElement(this.idMap_, element, "id", recurse);
            this.removeElement(this.nameMap_, element, "name", recurse);
        }

    }

    private void removeElement(Map<String, SortedSet<DomElement>> map, DomElement element, String attribute, boolean recurse) {
        String value = getAttributeValue(element, attribute);
        if (DomElement.ATTRIBUTE_NOT_DEFINED != value) {
            SortedSet<DomElement> elements = (SortedSet) map.remove(value);
            if (elements != null && (elements.size() != 1 || !elements.contains(element))) {
                elements.remove(element);
                map.put(value, elements);
            }
        }

        if (recurse) {
            Iterator var8 = element.getChildElements().iterator();

            while (var8.hasNext()) {
                DomElement child = (DomElement) var8.next();
                this.removeElement(map, child, attribute, true);
            }
        }

    }

    static boolean isMappedElement(Document document, String attributeName) {
        return document instanceof HtmlPage && ("name".equals(attributeName) || "id".equals(attributeName));
    }

    private void calculateBase() {
        List<HtmlElement> baseElements = this.getDocumentElement().getElementsByTagName("base");
        switch (baseElements.size()) {
            case 0:
                this.base_ = null;
                break;
            case 1:
                this.base_ = (HtmlBase) baseElements.get(0);
                break;
            default:
                this.base_ = (HtmlBase) baseElements.get(0);
                this.notifyIncorrectness("Multiple 'base' detected, only the first is used.");
        }

    }

    void loadFrames() throws FailingHttpStatusCodeException {
        Iterator var1 = this.getFrames().iterator();

        while (var1.hasNext()) {
            FrameWindow w = (FrameWindow) var1.next();
            BaseFrameElement frame = w.getFrameElement();
            if (frame.getEnclosedWindow() != null && WebClient.URL_ABOUT_BLANK == frame.getEnclosedPage().getUrl() && !frame.isContentLoaded()) {
                frame.loadInnerPage();
            }
        }

    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("HtmlPage(");
        builder.append(this.getUrl());
        builder.append(")@");
        builder.append(this.hashCode());
        return builder.toString();
    }

    protected List<HtmlMeta> getMetaTags(String httpEquiv) {
        if (this.getDocumentElement() == null) {
            return Collections.emptyList();
        } else {
            String nameLC = httpEquiv.toLowerCase(Locale.ROOT);
            List<HtmlMeta> tags = this.getDocumentElement().getElementsByTagNameImpl("meta");
            List<HtmlMeta> foundTags = new ArrayList();
            Iterator var5 = tags.iterator();

            while (var5.hasNext()) {
                HtmlMeta htmlMeta = (HtmlMeta) var5.next();
                if (nameLC.equals(htmlMeta.getHttpEquivAttribute().toLowerCase(Locale.ROOT))) {
                    foundTags.add(htmlMeta);
                }
            }

            return foundTags;
        }
    }

    protected HtmlPage clone() {
        HtmlPage result = (HtmlPage) super.clone();
        result.elementWithFocus_ = null;
        result.idMap_ = Collections.synchronizedMap(new HashMap());
        result.nameMap_ = Collections.synchronizedMap(new HashMap());
        return result;
    }

    public HtmlPage cloneNode(boolean deep) {
        HtmlPage result = (HtmlPage) super.cloneNode(false);
        SimpleScriptable jsObjClone = ((SimpleScriptable) this.getScriptableObject()).clone();
        jsObjClone.setDomNode(result);
        if (deep) {
            Object var4 = this.lock_;
            synchronized (this.lock_) {
                result.attributeListeners_ = null;
            }

            result.selectionRanges_ = new ArrayList(3);
            result.afterLoadActions_ = new ArrayList();
            result.frameElements_ = new TreeSet(documentPositionComparator);

            for (DomNode child = this.getFirstChild(); child != null; child = child.getNextSibling()) {
                result.appendChild(child.cloneNode(true));
            }
        }

        return result;
    }

    public void addHtmlAttributeChangeListener(HtmlAttributeChangeListener listener) {
        WebAssert.notNull("listener", listener);
        Object var2 = this.lock_;
        synchronized (this.lock_) {
            if (this.attributeListeners_ == null) {
                this.attributeListeners_ = new LinkedHashSet();
            }

            this.attributeListeners_.add(listener);
        }
    }

    public void removeHtmlAttributeChangeListener(HtmlAttributeChangeListener listener) {
        WebAssert.notNull("listener", listener);
        Object var2 = this.lock_;
        synchronized (this.lock_) {
            if (this.attributeListeners_ != null) {
                this.attributeListeners_.remove(listener);
            }

        }
    }

    void fireHtmlAttributeAdded(HtmlAttributeChangeEvent event) {
        List<HtmlAttributeChangeListener> listeners = this.safeGetAttributeListeners();
        if (listeners != null) {
            Iterator var3 = listeners.iterator();

            while (var3.hasNext()) {
                HtmlAttributeChangeListener listener = (HtmlAttributeChangeListener) var3.next();
                listener.attributeAdded(event);
            }
        }

    }

    void fireHtmlAttributeReplaced(HtmlAttributeChangeEvent event) {
        List<HtmlAttributeChangeListener> listeners = this.safeGetAttributeListeners();
        if (listeners != null) {
            Iterator var3 = listeners.iterator();

            while (var3.hasNext()) {
                HtmlAttributeChangeListener listener = (HtmlAttributeChangeListener) var3.next();
                listener.attributeReplaced(event);
            }
        }

    }

    void fireHtmlAttributeRemoved(HtmlAttributeChangeEvent event) {
        List<HtmlAttributeChangeListener> listeners = this.safeGetAttributeListeners();
        if (listeners != null) {
            Iterator var3 = listeners.iterator();

            while (var3.hasNext()) {
                HtmlAttributeChangeListener listener = (HtmlAttributeChangeListener) var3.next();
                listener.attributeRemoved(event);
            }
        }

    }

    private List<HtmlAttributeChangeListener> safeGetAttributeListeners() {
        Object var1 = this.lock_;
        synchronized (this.lock_) {
            return this.attributeListeners_ != null ? new ArrayList(this.attributeListeners_) : null;
        }
    }

    protected void checkChildHierarchy(Node newChild) throws DOMException {
        if (newChild instanceof Element) {
            if (this.getDocumentElement() != null) {
                throw new DOMException((short) 3, "The Document may only have a single child Element.");
            }
        } else if (newChild instanceof DocumentType) {
            if (this.getDoctype() != null) {
                throw new DOMException((short) 3, "The Document may only have a single child DocumentType.");
            }
        } else if (!(newChild instanceof Comment) && !(newChild instanceof ProcessingInstruction)) {
            throw new DOMException((short) 3, "The Document may not have a child of this type: " + newChild.getNodeType());
        }

        super.checkChildHierarchy(newChild);
    }

    public boolean isBeingParsed() {
        return this.parserCount_ > 0;
    }

    void registerParsingStart() {
        ++this.parserCount_;
    }

    void registerParsingEnd() {
        --this.parserCount_;
    }

    boolean isParsingHtmlSnippet() {
        return this.snippetParserCount_ > 0;
    }

    void registerSnippetParsingStart() {
        ++this.snippetParserCount_;
    }

    void registerSnippetParsingEnd() {
        --this.snippetParserCount_;
    }

    boolean isParsingInlineHtmlSnippet() {
        return this.inlineSnippetParserCount_ > 0;
    }

    void registerInlineSnippetParsingStart() {
        ++this.inlineSnippetParserCount_;
    }

    void registerInlineSnippetParsingEnd() {
        --this.inlineSnippetParserCount_;
    }

    public Page refresh() throws IOException {
        return this.getWebClient().getPage(this.getWebResponse().getWebRequest());
    }

    public void writeInParsedStream(String string) {
        this.builder_.pushInputString(string);
    }

    void setBuilder(HtmlUnitDOMBuilder htmlUnitDOMBuilder) {
        this.builder_ = htmlUnitDOMBuilder;
    }

    HtmlUnitDOMBuilder getBuilder() {
        return this.builder_;
    }

    public Map<String, String> getNamespaces() {
        NamedNodeMap attributes = this.getDocumentElement().getAttributes();
        Map<String, String> namespaces = new HashMap();

        for (int i = 0; i < attributes.getLength(); ++i) {
            Attr attr = (Attr) attributes.item(i);
            String name = attr.getName();
            if (name.startsWith("xmlns")) {
                int startPos = 5;
                if (name.length() > 5 && name.charAt(5) == ':') {
                    startPos = 6;
                }

                name = name.substring(startPos);
                namespaces.put(name, attr.getValue());
            }
        }

        return namespaces;
    }

    protected void setDocumentType(DocumentType type) {
        super.setDocumentType(type);
    }

    public void save(File file) throws IOException {
        (new XmlSerializer()).save(this, file);
    }

    public boolean isQuirksMode() {
        return "BackCompat".equals(((HTMLDocument) this.getScriptableObject()).getCompatMode());
    }

    public boolean isAttachedToPage() {
        return true;
    }

    public boolean isHtmlPage() {
        return true;
    }

    public URL getBaseURL() {
        URL baseUrl;
        if (this.base_ == null) {
            baseUrl = this.getUrl();
            WebWindow window = this.getEnclosingWindow();
            boolean frame = window != window.getTopWindow();
            if (frame) {
                boolean frameSrcIsNotSet = baseUrl == WebClient.URL_ABOUT_BLANK;
                boolean frameSrcIsJs = "javascript".equals(baseUrl.getProtocol());
                if (frameSrcIsNotSet || frameSrcIsJs) {
                    baseUrl = ((HtmlPage) window.getTopWindow().getEnclosedPage()).getWebResponse().getWebRequest().getUrl();
                }
            } else if (this.baseUrl_ != null) {
                baseUrl = this.baseUrl_;
            }
        } else {
            String href = this.base_.getHrefAttribute().trim();
            if (StringUtils.isEmpty(href)) {
                baseUrl = this.getUrl();
            } else {
                URL url = this.getUrl();

                try {
                    if (!href.startsWith("http://") && !href.startsWith("https://")) {
                        if (href.startsWith("//")) {
                            baseUrl = new URL(String.format("%s:%s", url.getProtocol(), href));
                        } else if (href.startsWith("/")) {
                            int port = Window.getPort(url);
                            baseUrl = new URL(String.format("%s://%s:%d%s", url.getProtocol(), url.getHost(), port, href));
                        } else if (url.toString().endsWith("/")) {
                            baseUrl = new URL(String.format("%s%s", url.toString(), href));
                        } else {
                            baseUrl = new URL(UrlUtils.resolveUrl(url, href));
                        }
                    } else {
                        baseUrl = new URL(href);
                    }
                } catch (MalformedURLException var6) {
                    this.notifyIncorrectness("Invalid base url: \"" + href + "\", ignoring it");
                    baseUrl = url;
                }
            }
        }

        return baseUrl;
    }

    public void addAutoCloseable(AutoCloseable autoCloseable) {
        if (autoCloseable != null) {
            if (this.autoCloseableList_ == null) {
                this.autoCloseableList_ = new ArrayList();
            }

            this.autoCloseableList_.add(autoCloseable);
        }
    }

    public boolean handles(Event event) {
        return !"blur".equals(event.getType()) && !"focus".equals(event.getType()) ? super.handles(event) : true;
    }

    public void setElementFromPointHandler(ElementFromPointHandler elementFromPointHandler) {
        this.elementFromPointHandler_ = elementFromPointHandler;
    }

    public HtmlElement getElementFromPoint(int x, int y) {
        if (this.elementFromPointHandler_ == null) {
            LOG.warn("ElementFromPointHandler was not specicifed for " + this);
            return x > 0 && y > 0 ? this.getBody() : null;
        } else {
            return this.elementFromPointHandler_.getElementFromPoint(this, x, y);
        }
    }

    public boolean setFocusedElement(DomElement newElement) {
        return this.setFocusedElement(newElement, false);
    }

    public boolean setFocusedElement(DomElement newElement, boolean windowActivated) {
        if (this.elementWithFocus_ == newElement && !windowActivated) {
            return true;
        } else {
            DomElement oldFocusedElement = this.elementWithFocus_;
            this.elementWithFocus_ = null;
            if (!windowActivated) {
                if (this.hasFeature(BrowserVersionFeatures.EVENT_FOCUS_IN_FOCUS_OUT_BLUR)) {
                    if (oldFocusedElement != null) {
                        oldFocusedElement.fireEvent("focusout");
                    }

                    if (newElement != null) {
                        newElement.fireEvent("focusin");
                    }
                }

                if (oldFocusedElement != null) {
                    oldFocusedElement.removeFocus();
                    oldFocusedElement.fireEvent("blur");
                }
            }

            this.elementWithFocus_ = newElement;
            if (this.elementWithFocus_ instanceof SelectableTextInput && this.hasFeature(BrowserVersionFeatures.PAGE_SELECTION_RANGE_FROM_SELECTABLE_TEXT_INPUT)) {
                SelectableTextInput sti = (SelectableTextInput) this.elementWithFocus_;
                this.setSelectionRange(new SimpleRange(sti, sti.getSelectionStart(), sti, sti.getSelectionEnd()));
            }

            if (this.elementWithFocus_ != null) {
                this.elementWithFocus_.focus();
                this.elementWithFocus_.fireEvent("focus");
            }

            if (this.hasFeature(BrowserVersionFeatures.EVENT_FOCUS_FOCUS_IN_BLUR_OUT)) {
                if (oldFocusedElement != null) {
                    oldFocusedElement.fireEvent("focusout");
                }

                if (newElement != null) {
                    newElement.fireEvent("focusin");
                }
            }

            return this == this.getEnclosingWindow().getEnclosedPage();
        }
    }

    public DomElement getFocusedElement() {
        return this.elementWithFocus_;
    }

    public void setElementWithFocus(DomElement elementWithFocus) {
        this.elementWithFocus_ = elementWithFocus;
    }

    public List<Range> getSelectionRanges() {
        return this.selectionRanges_;
    }

    public void setSelectionRange(Range selectionRange) {
        this.selectionRanges_.clear();
        this.selectionRanges_.add(selectionRange);
    }

    public ScriptResult executeJavaScriptFunction(Object function, Object thisObject, Object[] args, DomNode htmlElementScope) {
        return !this.getWebClient().getOptions().isJavaScriptEnabled() ? new ScriptResult((Object) null, this) : this.executeJavaScriptFunction((Function) function, (Scriptable) thisObject, args, htmlElementScope);
    }

    private ScriptResult executeJavaScriptFunction(Function function, Scriptable thisObject, Object[] args, DomNode htmlElementScope) {
        JavaScriptEngine engine = (JavaScriptEngine) this.getWebClient().getJavaScriptEngine();
        Object result = engine.callFunction(this, function, thisObject, args, htmlElementScope);
        return new ScriptResult(result, this.getWebClient().getCurrentWindow().getEnclosedPage());
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeObject(this.originalCharset_ == null ? null : this.originalCharset_.name());
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        String charsetName = (String) ois.readObject();
        if (charsetName != null) {
            this.originalCharset_ = Charset.forName(charsetName);
        }

    }

    static enum JavaScriptLoadResult {
        NOOP,
        SUCCESS,
        DOWNLOAD_ERROR,
        COMPILATION_ERROR;

        private JavaScriptLoadResult() {
        }
    }

    static class DocumentPositionComparator implements Comparator<DomElement>, Serializable {
        DocumentPositionComparator() {
        }

        public int compare(DomElement elt1, DomElement elt2) {
            short relation = elt1.compareDocumentPosition(elt2);
            if (relation == 0) {
                return 0;
            } else {
                return (relation & 8) == 0 && (relation & 2) == 0 ? -1 : 1;
            }
        }
    }
}
