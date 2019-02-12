//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.gargoylesoftware.htmlunit;

import com.gargoylesoftware.css.parser.CSSErrorHandler;
import com.gargoylesoftware.htmlunit.DownloadedContent.OnFile;
import com.gargoylesoftware.htmlunit.activex.javascript.msxml.MSXMLActiveXObjectFactory;
import com.gargoylesoftware.htmlunit.attachment.Attachment;
import com.gargoylesoftware.htmlunit.attachment.AttachmentHandler;
import com.gargoylesoftware.htmlunit.html.BaseFrameElement;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.FrameWindow;
import com.gargoylesoftware.htmlunit.html.HTMLParserListener;
import com.gargoylesoftware.htmlunit.html.HtmlInlineFrame;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.httpclient.HtmlUnitBrowserCompatCookieSpec;
import com.gargoylesoftware.htmlunit.javascript.AbstractJavaScriptEngine;
import com.gargoylesoftware.htmlunit.javascript.DefaultJavaScriptErrorListener;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptEngine;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;
import com.gargoylesoftware.htmlunit.javascript.background.JavaScriptJobManager;
import com.gargoylesoftware.htmlunit.javascript.host.Location;
import com.gargoylesoftware.htmlunit.javascript.host.Window;
import com.gargoylesoftware.htmlunit.javascript.host.css.ComputedCSSStyleDeclaration;
import com.gargoylesoftware.htmlunit.javascript.host.dom.Node;
import com.gargoylesoftware.htmlunit.javascript.host.event.Event;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLDocument;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLElement;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLIFrameElement;
import com.gargoylesoftware.htmlunit.protocol.data.DataURLConnection;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.gargoylesoftware.htmlunit.util.UrlUtils;
import com.gargoylesoftware.htmlunit.webstart.WebStartHandler;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

import net.sourceforge.htmlunit.corejs.javascript.ScriptableObject;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.io.CopyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.message.BufferedHeader;
import org.apache.http.util.CharArrayBuffer;
import sun.swing.BeanInfoUtils;

public class WebClient implements Serializable, AutoCloseable {
    private static final Log LOG = LogFactory.getLog(WebClient.class);
    private static final int ALLOWED_REDIRECTIONS_SAME_URL = 20;
    private transient WebConnection webConnection_;
    private CredentialsProvider credentialsProvider_;
    private CookieManager cookieManager_;
    private transient AbstractJavaScriptEngine<?> scriptEngine_;
    private transient List<WebClient.LoadJob> loadQueue_;
    private final Map<String, String> requestHeaders_;
    private IncorrectnessListener incorrectnessListener_;
    private WebConsole webConsole_;
    private AlertHandler alertHandler_;
    private ConfirmHandler confirmHandler_;
    private PromptHandler promptHandler_;
    private StatusHandler statusHandler_;
    private AttachmentHandler attachmentHandler_;
    private WebStartHandler webStartHandler_;
    private AppletConfirmHandler appletConfirmHandler_;
    private AjaxController ajaxController_;
    private BrowserVersion browserVersion_;
    private PageCreator pageCreator_;
    private final Set<WebWindowListener> webWindowListeners_;
    private final Deque<TopLevelWindow> topLevelWindows_;
    private final List<WebWindow> windows_;
    private transient List<WeakReference<JavaScriptJobManager>> jobManagers_;
    private WebWindow currentWindow_;
    private HTMLParserListener htmlParserListener_;
    private CSSErrorHandler cssErrorHandler_;
    private OnbeforeunloadHandler onbeforeunloadHandler_;
    private Cache cache_;
    private static final String TARGET_BLANK = "_blank";
    private static final String TARGET_SELF = "_self";
    private static final String TARGET_PARENT = "_parent";
    private static final String TARGET_TOP = "_top";
    public static final String ABOUT_SCHEME = "about:";
    public static final String ABOUT_BLANK = "about:blank";
    public static final URL URL_ABOUT_BLANK = UrlUtils.toUrlSafe("about:blank");
    private ScriptPreProcessor scriptPreProcessor_;
    private Map<String, String> activeXObjectMap_;
    private transient MSXMLActiveXObjectFactory msxmlActiveXObjectFactory_;
    private RefreshHandler refreshHandler_;
    private JavaScriptErrorListener javaScriptErrorListener_;
    private WebClientOptions options_;
    private WebClientInternals internals_;
    private final StorageHolder storageHolder_;
    private static final WebResponseData responseDataNoHttpResponse_ = new WebResponseData(0, "No HTTP Response", Collections.emptyList());
    private static final ThreadLocal<Map<String, WebResponse>> cacheResponse = new ThreadLocal<>();

    public WebClient() {
        this(BrowserVersion.getDefault());
    }

    public WebClient(BrowserVersion browserVersion) {
        this(browserVersion, (String) null, -1);
    }

    public WebClient(BrowserVersion browserVersion, String proxyHost, int proxyPort) {
        this.credentialsProvider_ = new DefaultCredentialsProvider();
        this.cookieManager_ = new CookieManager();
        this.requestHeaders_ = Collections.synchronizedMap(new HashMap(89));
        this.incorrectnessListener_ = new IncorrectnessListenerImpl();
        this.ajaxController_ = new AjaxController();
        this.pageCreator_ = new DefaultPageCreator();
        this.webWindowListeners_ = new HashSet(5);
        this.topLevelWindows_ = new ArrayDeque();
        this.windows_ = Collections.synchronizedList(new ArrayList());
        this.jobManagers_ = Collections.synchronizedList(new ArrayList());
        this.cssErrorHandler_ = new DefaultCssErrorHandler();
        this.cache_ = new Cache();
        this.activeXObjectMap_ = Collections.emptyMap();
        this.refreshHandler_ = new NiceRefreshHandler(2);
        this.javaScriptErrorListener_ = new DefaultJavaScriptErrorListener();
        this.options_ = new WebClientOptions();
        this.internals_ = new WebClientInternals(this);
        this.storageHolder_ = new StorageHolder();
        WebAssert.notNull("browserVersion", browserVersion);
        this.browserVersion_ = browserVersion;
        if (proxyHost == null) {
            this.getOptions().setProxyConfig(new ProxyConfig());
        } else {
            this.getOptions().setProxyConfig(new ProxyConfig(proxyHost, proxyPort));
        }

        this.webConnection_ = new HttpWebConnection(this);
        this.scriptEngine_ = new JavaScriptEngine(this);
        this.loadQueue_ = new ArrayList();
        this.addWebWindowListener(new WebClient.CurrentWindowTracker(this));
        this.currentWindow_ = new TopLevelWindow("", this);
        this.fireWindowOpened(new WebWindowEvent(this.currentWindow_, 1, (Page) null, (Page) null));
        if (this.getBrowserVersion().hasFeature(BrowserVersionFeatures.JS_XML_SUPPORT_VIA_ACTIVEXOBJECT)) {
            this.initMSXMLActiveX();
        }

    }

    private void initMSXMLActiveX() {
        this.msxmlActiveXObjectFactory_ = new MSXMLActiveXObjectFactory();

        try {
            this.msxmlActiveXObjectFactory_.init(this.getBrowserVersion());
        } catch (Exception var2) {
            LOG.error("Exception while initializing MSXML ActiveX for the page", var2);
            throw new ScriptException((HtmlPage) null, var2);
        }
    }

    public WebConnection getWebConnection() {
        return this.webConnection_;
    }

    public void setWebConnection(WebConnection webConnection) {
        WebAssert.notNull("webConnection", webConnection);
        this.webConnection_ = webConnection;
    }

    public <P extends Page> P getPage(WebWindow webWindow, WebRequest webRequest) throws IOException, FailingHttpStatusCodeException {
        return this.getPage(webWindow, webRequest, true);
    }

    <P extends Page> P getPage(WebWindow webWindow, WebRequest webRequest, boolean addToHistory) throws IOException, FailingHttpStatusCodeException {
        Page page = webWindow.getEnclosedPage();
        if (page != null) {
            URL prev = page.getUrl();
            URL current = webRequest.getUrl();
            if (UrlUtils.sameFile(current, prev) && current.getRef() != null && !StringUtils.equals(current.getRef(), prev.getRef())) {
                page.getWebResponse().getWebRequest().setUrl(current);
                if (addToHistory) {
                    webWindow.getHistory().addPage(page);
                }

                Window window = (Window) webWindow.getScriptableObject();
                if (window != null) {
                    window.getLocation().setHash(current.getRef());
                    window.clearComputedStyles();
                }

                return (P) page;
            }

            if (page.isHtmlPage()) {
                HtmlPage htmlPage = (HtmlPage) page;
                if (!htmlPage.isOnbeforeunloadAccepted()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("The registered OnbeforeunloadHandler rejected to load a new page.");
                    }

                    return (P) page;
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Get page for window named '" + webWindow.getName() + "', using " + webRequest);
        }

        String protocol = webRequest.getUrl().getProtocol();
        WebResponse webResponse;
        if ("javascript".equals(protocol)) {
            webResponse = this.makeWebResponseForJavaScriptUrl(webWindow, webRequest.getUrl(), webRequest.getCharset());
            if (webWindow.getEnclosedPage() != null && webWindow.getEnclosedPage().getWebResponse() == webResponse) {
                return (P) webWindow.getEnclosedPage();
            }
        } else {
            webResponse = this.loadWebResponse(webRequest);
        }

        this.printContentIfNecessary(webResponse);
        this.loadWebResponseInto(webResponse, webWindow);
        if (this.scriptEngine_ != null) {
            this.scriptEngine_.registerWindowAndMaybeStartEventLoop(webWindow);
        }

        this.throwFailingHttpStatusCodeExceptionIfNecessary(webResponse);
        return (P) webWindow.getEnclosedPage();
    }

    public <P extends Page> P getPage(WebWindow opener, String target, WebRequest params) throws FailingHttpStatusCodeException, IOException {
        return this.getPage(this.openTargetWindow(opener, target, "_self"), params);
    }

    public <P extends Page> P getPage(String url) throws IOException, FailingHttpStatusCodeException, MalformedURLException {
        return this.getPage(UrlUtils.toUrlUnsafe(url));
    }

    public <P extends Page> P getPage(URL url) throws IOException, FailingHttpStatusCodeException {
        WebRequest request = new WebRequest(url, this.getBrowserVersion().getHtmlAcceptHeader());
        request.setCharset(StandardCharsets.UTF_8);
        return this.getPage(this.getCurrentWindow().getTopWindow(), request);
    }

    public <P extends Page> P getPage(WebRequest request) throws IOException, FailingHttpStatusCodeException {
        return this.getPage(this.getCurrentWindow().getTopWindow(), request);
    }

    public Page loadWebResponseInto(WebResponse webResponse, WebWindow webWindow) throws IOException, FailingHttpStatusCodeException {
        WebAssert.notNull("webResponse", webResponse);
        WebAssert.notNull("webWindow", webWindow);
        if (webResponse.getStatusCode() == 204) {
            return webWindow.getEnclosedPage();
        } else if (this.webStartHandler_ != null && "application/x-java-jnlp-file".equals(webResponse.getContentType())) {
            this.webStartHandler_.handleJnlpResponse(webResponse);
            return webWindow.getEnclosedPage();
        } else {
            Page newPage;
            if (this.attachmentHandler_ != null && Attachment.isAttachment(webResponse)) {
                WebWindow w = this.openWindow((URL) null, (String) null, webWindow);
                newPage = this.pageCreator_.createPage(webResponse, w);
                this.attachmentHandler_.handleAttachment(newPage);
                return newPage;
            } else {
                Page oldPage = webWindow.getEnclosedPage();
                if (oldPage != null) {
                    oldPage.cleanUp();
                }

                newPage = null;
                if (this.windows_.contains(webWindow) || this.getBrowserVersion().hasFeature(BrowserVersionFeatures.WINDOW_EXECUTE_EVENTS)) {
                    newPage = this.pageCreator_.createPage(webResponse, webWindow);
                    if (this.windows_.contains(webWindow)) {
                        this.fireWindowContentChanged(new WebWindowEvent(webWindow, 3, oldPage, newPage));
                        if (webWindow.getEnclosedPage() == newPage) {
                            newPage.initialize();
                            if (webWindow instanceof FrameWindow && !newPage.isHtmlPage()) {
                                FrameWindow fw = (FrameWindow) webWindow;
                                BaseFrameElement frame = fw.getFrameElement();
                                if (frame.hasEventHandlers("onload")) {
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug("Executing onload handler for " + frame);
                                    }

                                    Event event = new Event(frame, "load");
                                    ((Node) frame.getScriptableObject()).executeEventLocally(event);
                                }
                            }
                        }
                    }
                }

                return newPage;
            }
        }
    }

    public void printContentIfNecessary(WebResponse webResponse) {
        if (this.getOptions().isPrintContentOnFailingStatusCode()) {
            int statusCode = webResponse.getStatusCode();
            boolean successful = statusCode >= 200 && statusCode < 300;
            if (!successful) {
                String contentType = webResponse.getContentType();
                LOG.info("statusCode=[" + statusCode + "] contentType=[" + contentType + "]");
                LOG.info(webResponse.getContentAsString());
            }
        }

    }

    public void throwFailingHttpStatusCodeExceptionIfNecessary(WebResponse webResponse) {
        int statusCode = webResponse.getStatusCode();
        boolean successful = statusCode >= 200 && statusCode < 300 || statusCode == 305 || statusCode == 304;
        if (this.getOptions().isThrowExceptionOnFailingStatusCode() && !successful) {
            throw new FailingHttpStatusCodeException(webResponse);
        }
    }

    public void addRequestHeader(String name, String value) {
        if ("cookie".equalsIgnoreCase(name)) {
            throw new IllegalArgumentException("Do not add 'Cookie' header, use .getCookieManager() instead");
        } else {
            this.requestHeaders_.put(name, value);
        }
    }

    public void removeRequestHeader(String name) {
        this.requestHeaders_.remove(name);
    }

    public void setCredentialsProvider(CredentialsProvider credentialsProvider) {
        WebAssert.notNull("credentialsProvider", credentialsProvider);
        this.credentialsProvider_ = credentialsProvider;
    }

    public CredentialsProvider getCredentialsProvider() {
        return this.credentialsProvider_;
    }

    public AbstractJavaScriptEngine<?> getJavaScriptEngine() {
        return this.scriptEngine_;
    }

    public void setJavaScriptEngine(AbstractJavaScriptEngine<?> engine) {
        if (engine == null) {
            throw new IllegalArgumentException("Can't set JavaScriptEngine to null");
        } else {
            this.scriptEngine_ = engine;
        }
    }

    public CookieManager getCookieManager() {
        return this.cookieManager_;
    }

    public void setCookieManager(CookieManager cookieManager) {
        WebAssert.notNull("cookieManager", cookieManager);
        this.cookieManager_ = cookieManager;
    }

    public void setAlertHandler(AlertHandler alertHandler) {
        this.alertHandler_ = alertHandler;
    }

    public AlertHandler getAlertHandler() {
        return this.alertHandler_;
    }

    public void setConfirmHandler(ConfirmHandler handler) {
        this.confirmHandler_ = handler;
    }

    public ConfirmHandler getConfirmHandler() {
        return this.confirmHandler_;
    }

    public void setPromptHandler(PromptHandler handler) {
        this.promptHandler_ = handler;
    }

    public PromptHandler getPromptHandler() {
        return this.promptHandler_;
    }

    public void setStatusHandler(StatusHandler statusHandler) {
        this.statusHandler_ = statusHandler;
    }

    public StatusHandler getStatusHandler() {
        return this.statusHandler_;
    }

    public void setJavaScriptErrorListener(JavaScriptErrorListener javaScriptErrorListener) {
        if (javaScriptErrorListener == null) {
            this.javaScriptErrorListener_ = new DefaultJavaScriptErrorListener();
        } else {
            this.javaScriptErrorListener_ = javaScriptErrorListener;
        }

    }

    public JavaScriptErrorListener getJavaScriptErrorListener() {
        return this.javaScriptErrorListener_;
    }

    public BrowserVersion getBrowserVersion() {
        return this.browserVersion_;
    }

    public WebWindow getCurrentWindow() {
        return this.currentWindow_;
    }

    public void setCurrentWindow(WebWindow window) {
        WebAssert.notNull("window", window);
        if (this.currentWindow_ != window) {
            if (this.currentWindow_ != null && !this.currentWindow_.isClosed()) {
                Page enclosedPage = this.currentWindow_.getEnclosedPage();
                if (enclosedPage != null && enclosedPage.isHtmlPage()) {
                    DomElement focusedElement = ((HtmlPage) enclosedPage).getFocusedElement();
                    if (focusedElement != null) {
                        focusedElement.fireEvent("blur");
                    }
                }
            }

            this.currentWindow_ = window;
            boolean isIFrame = this.currentWindow_ instanceof FrameWindow && ((FrameWindow) this.currentWindow_).getFrameElement() instanceof HtmlInlineFrame;
            if (!isIFrame) {
                Page enclosedPage = this.currentWindow_.getEnclosedPage();
                if (enclosedPage != null && enclosedPage.isHtmlPage()) {
                    Object jsWindow = this.currentWindow_.getScriptableObject();
                    if (jsWindow instanceof Window) {
                        HTMLElement activeElement = ((HTMLDocument) ((Window) jsWindow).getDocument()).getActiveElement();
                        if (activeElement != null) {
                            ((HtmlPage) enclosedPage).setFocusedElement(activeElement.getDomNodeOrDie(), true);
                        }
                    }
                }
            }

        }
    }

    public void addWebWindowListener(WebWindowListener listener) {
        WebAssert.notNull("listener", listener);
        this.webWindowListeners_.add(listener);
    }

    public void removeWebWindowListener(WebWindowListener listener) {
        WebAssert.notNull("listener", listener);
        this.webWindowListeners_.remove(listener);
    }

    private void fireWindowContentChanged(WebWindowEvent event) {
        Iterator var2 = (new ArrayList(this.webWindowListeners_)).iterator();

        while (var2.hasNext()) {
            WebWindowListener listener = (WebWindowListener) var2.next();
            listener.webWindowContentChanged(event);
        }

    }

    private void fireWindowOpened(WebWindowEvent event) {
        Iterator var2 = (new ArrayList(this.webWindowListeners_)).iterator();

        while (var2.hasNext()) {
            WebWindowListener listener = (WebWindowListener) var2.next();
            listener.webWindowOpened(event);
        }

    }

    private void fireWindowClosed(WebWindowEvent event) {
        Iterator var2 = (new ArrayList(this.webWindowListeners_)).iterator();

        while (var2.hasNext()) {
            WebWindowListener listener = (WebWindowListener) var2.next();
            listener.webWindowClosed(event);
        }

    }

    public WebWindow openWindow(URL url, String windowName) {
        WebAssert.notNull("windowName", windowName);
        return this.openWindow(url, windowName, this.getCurrentWindow());
    }

    public WebWindow openWindow(URL url, String windowName, WebWindow opener) {
        WebWindow window = this.openTargetWindow(opener, windowName, "_blank");
        HtmlPage openerPage = (HtmlPage) opener.getEnclosedPage();
        if (url != null) {
            try {
                WebRequest request = new WebRequest(url, this.getBrowserVersion().getHtmlAcceptHeader());
                request.setCharset(StandardCharsets.UTF_8);
                if (this.getBrowserVersion().hasFeature(BrowserVersionFeatures.DIALOGWINDOW_REFERER) && openerPage != null) {
                    String referer = openerPage.getUrl().toExternalForm();
                    request.setAdditionalHeader("Referer", referer);
                }

                this.getPage(window, request);
            } catch (IOException var8) {
                LOG.error("Error loading content into window", var8);
            }
        } else {
            this.initializeEmptyWindow(window);
        }

        return window;
    }

    public WebWindow openTargetWindow(WebWindow opener, String windowName, String defaultName) {
        WebAssert.notNull("opener", opener);
        WebAssert.notNull("defaultName", defaultName);
        String windowToOpen = windowName;
        if (windowName == null || windowName.isEmpty()) {
            windowToOpen = defaultName;
        }

        WebWindow webWindow = this.resolveWindow(opener, windowToOpen);
        if (webWindow == null) {
            if ("_blank".equals(windowToOpen)) {
                windowToOpen = "";
            }

            webWindow = new TopLevelWindow(windowToOpen, this);
            this.fireWindowOpened(new WebWindowEvent((WebWindow) webWindow, 1, (Page) null, (Page) null));
        }

        if (webWindow instanceof TopLevelWindow && webWindow != opener.getTopWindow()) {
            ((TopLevelWindow) webWindow).setOpener(opener);
        }

        return (WebWindow) webWindow;
    }

    private WebWindow resolveWindow(WebWindow opener, String name) {
        if (name != null && !name.isEmpty() && !"_self".equals(name)) {
            if ("_parent".equals(name)) {
                return opener.getParentWindow();
            } else if ("_top".equals(name)) {
                return opener.getTopWindow();
            } else if ("_blank".equals(name)) {
                return null;
            } else {
                WebWindow window = opener;

                while (true) {
                    Page page = window.getEnclosedPage();
                    if (page != null && page.isHtmlPage()) {
                        try {
                            FrameWindow frame = ((HtmlPage) page).getFrameByName(name);
                            ScriptableObject scriptable = (ScriptableObject) frame.getFrameElement().getScriptableObject();
                            if (scriptable instanceof HTMLIFrameElement) {
                                ((HTMLIFrameElement) scriptable).onRefresh();
                            }

                            return frame;
                        } catch (ElementNotFoundException var8) {
                            ;
                        }
                    }

                    if (window == window.getParentWindow()) {
                        try {
                            return this.getWebWindowByName(name);
                        } catch (WebWindowNotFoundException var7) {
                            return null;
                        }
                    }

                    window = window.getParentWindow();
                }
            }
        } else {
            return opener;
        }
    }

    public DialogWindow openDialogWindow(URL url, WebWindow opener, Object dialogArguments) throws IOException {
        WebAssert.notNull("url", url);
        WebAssert.notNull("opener", opener);
        DialogWindow window = new DialogWindow(this, dialogArguments);
        this.fireWindowOpened(new WebWindowEvent(window, 1, (Page) null, (Page) null));
        HtmlPage openerPage = (HtmlPage) opener.getEnclosedPage();
        WebRequest request = new WebRequest(url, this.getBrowserVersion().getHtmlAcceptHeader());
        request.setCharset(StandardCharsets.UTF_8);
        if (this.getBrowserVersion().hasFeature(BrowserVersionFeatures.DIALOGWINDOW_REFERER) && openerPage != null) {
            String referer = openerPage.getUrl().toExternalForm();
            request.setAdditionalHeader("Referer", referer);
        }

        this.getPage(window, request);
        return window;
    }

    public void setPageCreator(PageCreator pageCreator) {
        WebAssert.notNull("pageCreator", pageCreator);
        this.pageCreator_ = pageCreator;
    }

    public PageCreator getPageCreator() {
        return this.pageCreator_;
    }

    public WebWindow getWebWindowByName(String name) throws WebWindowNotFoundException {
        WebAssert.notNull("name", name);
        Iterator var2 = this.windows_.iterator();

        WebWindow webWindow;
        do {
            if (!var2.hasNext()) {
                throw new WebWindowNotFoundException(name);
            }

            webWindow = (WebWindow) var2.next();
        } while (!name.equals(webWindow.getName()));

        return webWindow;
    }

    public void initialize(WebWindow webWindow) {
        WebAssert.notNull("webWindow", webWindow);
        this.scriptEngine_.initialize(webWindow);
    }

    public void initialize(Page newPage) {
        WebAssert.notNull("newPage", newPage);
        WebWindow webWindow = newPage.getEnclosingWindow();
        if (webWindow.getScriptableObject() instanceof Window) {
            ((Window) webWindow.getScriptableObject()).initialize(newPage);
        }

    }

    public void initializeEmptyWindow(WebWindow webWindow) {
        WebAssert.notNull("webWindow", webWindow);
        this.initialize(webWindow);
        ((Window) webWindow.getScriptableObject()).initialize();
    }

    public void registerWebWindow(WebWindow webWindow) {
        WebAssert.notNull("webWindow", webWindow);
        this.windows_.add(webWindow);
        this.jobManagers_.add(new WeakReference(webWindow.getJobManager()));
    }

    public void deregisterWebWindow(WebWindow webWindow) {
        WebAssert.notNull("webWindow", webWindow);
        if (this.windows_.remove(webWindow)) {
            this.fireWindowClosed(new WebWindowEvent(webWindow, 2, webWindow.getEnclosedPage(), (Page) null));
        }

    }

    public static URL expandUrl(URL baseUrl, String relativeUrl) throws MalformedURLException {
        String newUrl = UrlUtils.resolveUrl(baseUrl, relativeUrl);
        return UrlUtils.toUrlUnsafe(newUrl);
    }

    private WebResponse makeWebResponseForDataUrl(WebRequest webRequest) throws IOException {
        URL url = webRequest.getUrl();
        ArrayList responseHeaders = new ArrayList();

        DataURLConnection connection;
        try {
            connection = new DataURLConnection(url);
        } catch (DecoderException var20) {
            throw new IOException(var20.getMessage());
        }

        responseHeaders.add(new NameValuePair("content-type", connection.getMediaType() + ";charset=" + connection.getCharset()));
        InputStream is = connection.getInputStream();
        Throwable var6 = null;

        WebResponse var9;
        try {
            DownloadedContent downloadedContent = HttpWebConnection.downloadContent(is, this.getOptions().getMaxInMemory());
            WebResponseData data = new WebResponseData(downloadedContent, 200, "OK", responseHeaders);
            var9 = new WebResponse(data, url, webRequest.getHttpMethod(), 0L);
        } catch (Throwable var19) {
            var6 = var19;
            throw var19;
        } finally {
            if (is != null) {
                if (var6 != null) {
                    try {
                        is.close();
                    } catch (Throwable var18) {
                        var6.addSuppressed(var18);
                    }
                } else {
                    is.close();
                }
            }

        }

        return var9;
    }

    private static WebResponse makeWebResponseForAboutUrl(URL url) {
        String urlWithoutQuery = StringUtils.substringBefore(url.toExternalForm(), "?");
        if (!"blank".equalsIgnoreCase(StringUtils.substringAfter(urlWithoutQuery, "about:"))) {
            throw new IllegalArgumentException(url + " is not supported, only about:blank is supported now.");
        } else {
            return new StringWebResponse("", URL_ABOUT_BLANK);
        }
    }

    private WebResponse makeWebResponseForFileUrl(WebRequest webRequest) throws IOException {
        URL cleanUrl = webRequest.getUrl();
        if (cleanUrl.getQuery() != null) {
            cleanUrl = UrlUtils.getUrlWithNewQuery(cleanUrl, (String) null);
        }

        if (cleanUrl.getRef() != null) {
            cleanUrl = UrlUtils.getUrlWithNewRef(cleanUrl, (String) null);
        }

        WebResponse fromCache = this.getCache().getCachedResponse(webRequest);
        if (fromCache != null) {
            return new WebResponseFromCache(fromCache, webRequest);
        } else {
            String fileUrl = cleanUrl.toExternalForm();
            fileUrl = URLDecoder.decode(fileUrl, StandardCharsets.UTF_8.name());
            File file = new File(fileUrl.substring(5));
            if (!file.exists()) {
                List<NameValuePair> compiledHeaders = new ArrayList();
                compiledHeaders.add(new NameValuePair("Content-Type", "text/html"));
                WebResponseData responseData = new WebResponseData(TextUtil.stringToByteArray("File: " + file.getAbsolutePath(), StandardCharsets.UTF_8), 404, "Not Found", compiledHeaders);
                return new WebResponse(responseData, webRequest, 0L);
            } else {
                String contentType = this.guessContentType(file);
                DownloadedContent content = new OnFile(file, false);
                List<NameValuePair> compiledHeaders = new ArrayList();
                compiledHeaders.add(new NameValuePair("Content-Type", contentType));
                compiledHeaders.add(new NameValuePair("Last-Modified", DateUtils.formatDate(new Date(file.lastModified()))));
                WebResponseData responseData = new WebResponseData(content, 200, "OK", compiledHeaders);
                WebResponse webResponse = new WebResponse(responseData, webRequest, 0L);
                this.getCache().cacheIfPossible(webRequest, webResponse, (Object) null);
                return webResponse;
            }
        }
    }

    public String guessContentType(File file) {
        String fileName = file.getName();
        if (fileName.endsWith(".xhtml")) {
            return "application/xhtml+xml";
        } else if (fileName.endsWith(".js")) {
            return "application/javascript";
        } else if (fileName.toLowerCase(Locale.ROOT).endsWith(".css")) {
            return "text/css";
        } else {
            String contentType = URLConnection.guessContentTypeFromName(fileName);
            if (contentType == null) {
                try {
                    InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                    Throwable var5 = null;

                    try {
                        contentType = URLConnection.guessContentTypeFromStream(inputStream);
                    } catch (Throwable var15) {
                        var5 = var15;
                        throw var15;
                    } finally {
                        if (inputStream != null) {
                            if (var5 != null) {
                                try {
                                    inputStream.close();
                                } catch (Throwable var14) {
                                    var5.addSuppressed(var14);
                                }
                            } else {
                                inputStream.close();
                            }
                        }

                    }
                } catch (IOException var17) {
                    ;
                }
            }

            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return contentType;
        }
    }

    private WebResponse makeWebResponseForJavaScriptUrl(WebWindow webWindow, URL url, Charset charset) throws FailingHttpStatusCodeException, IOException {
        HtmlPage page = null;
        if (webWindow instanceof FrameWindow) {
            FrameWindow frameWindow = (FrameWindow) webWindow;
            page = (HtmlPage) frameWindow.getEnclosedPage();
        } else {
            Page currentPage = webWindow.getEnclosedPage();
            if (currentPage instanceof HtmlPage) {
                page = (HtmlPage) currentPage;
            }
        }

        if (page == null) {
            page = (HtmlPage) this.getPage(webWindow, new WebRequest(URL_ABOUT_BLANK));
        }

        ScriptResult r = page.executeJavaScript(url.toExternalForm(), "JavaScript URL", 1);
        if (r.getJavaScriptResult() != null && !ScriptResult.isUndefined(r)) {
            String contentString = r.getJavaScriptResult().toString();
            StringWebResponse response = new StringWebResponse(contentString, charset, url);
            response.setFromJavascript(true);
            return response;
        } else {
            return webWindow.getEnclosedPage().getWebResponse();
        }
    }

    public WebResponse loadWebResponse(WebRequest webRequest) throws IOException {
        String var2 = webRequest.getUrl().getProtocol();
        byte var3 = -1;
        switch (var2.hashCode()) {
            case 3076010:
                if (var2.equals("data")) {
                    var3 = 2;
                }
                break;
            case 3143036:
                if (var2.equals("file")) {
                    var3 = 1;
                }
                break;
            case 92611469:
                if (var2.equals("about")) {
                    var3 = 0;
                }
        }

        switch (var3) {
            case 0:
                return makeWebResponseForAboutUrl(webRequest.getUrl());
            case 1:
                return this.makeWebResponseForFileUrl(webRequest);
            case 2:
                return this.makeWebResponseForDataUrl(webRequest);
            default:
                return this.loadWebResponseFromWebConnection(webRequest, 20);
        }
    }

    private WebResponse loadWebResponseFromWebConnection(WebRequest webRequest, int allowedRedirects) throws IOException {
        URL url = webRequest.getUrl();


//        if (cacheResponse.get() == null || cacheResponse.get().size() == 0) {
//            cacheResponse.set(new HashMap<>());
//        }
//        if (cacheResponse.get().containsKey(url.toExternalForm())) {
//            return cacheResponse.get().get(url.toExternalForm());
//        }

        HttpMethod method = webRequest.getHttpMethod();
        List<NameValuePair> parameters = webRequest.getRequestParameters();
        WebAssert.notNull("url", url);
        WebAssert.notNull("method", method);
        WebAssert.notNull("parameters", parameters);

        url = UrlUtils.encodeUrl(url, this.getBrowserVersion().hasFeature(BrowserVersionFeatures.URL_MINIMAL_QUERY_ENCODING), webRequest.getCharset());
        webRequest.setUrl(url);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Load response for " + method + " " + url.toExternalForm());
        }

        if (webRequest.getProxyHost() == null) {
            ProxyConfig proxyConfig = this.getOptions().getProxyConfig();
            if (proxyConfig.getProxyAutoConfigUrl() != null) {
                if (!UrlUtils.sameFile(new URL(proxyConfig.getProxyAutoConfigUrl()), url)) {
                    String content = proxyConfig.getProxyAutoConfigContent();
                    if (content == null) {
                        content = this.getPage(proxyConfig.getProxyAutoConfigUrl()).getWebResponse().getContentAsString();
                        proxyConfig.setProxyAutoConfigContent(content);
                    }

                    String allValue = ProxyAutoConfig.evaluate(content, url);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Proxy Auto-Config: value '" + allValue + "' for URL " + url);
                    }

                    String value = allValue.split(";")[0].trim();
                    int colonIndex;
                    if (value.startsWith("PROXY")) {
                        value = value.substring(6);
                        colonIndex = value.indexOf(58);
                        webRequest.setSocksProxy(false);
                        webRequest.setProxyHost(value.substring(0, colonIndex));
                        webRequest.setProxyPort(Integer.parseInt(value.substring(colonIndex + 1)));
                    } else if (value.startsWith("SOCKS")) {
                        value = value.substring(6);
                        colonIndex = value.indexOf(58);
                        webRequest.setSocksProxy(true);
                        webRequest.setProxyHost(value.substring(0, colonIndex));
                        webRequest.setProxyPort(Integer.parseInt(value.substring(colonIndex + 1)));
                    }
                }
            } else if (!proxyConfig.shouldBypassProxy(webRequest.getUrl().getHost())) {
                webRequest.setProxyHost(proxyConfig.getProxyHost());
                webRequest.setProxyPort(proxyConfig.getProxyPort());
                webRequest.setSocksProxy(proxyConfig.isSocksProxy());
            }
        }

        this.addDefaultHeaders(webRequest);
        WebResponse fromCache = this.getCache().getCachedResponse(webRequest);
        Object webResponse;
        if (fromCache != null) {
            LOG.debug("Loading [ " + url.toExternalForm() + " ] from cache successfully!");
            webResponse = new WebResponseFromCache(fromCache, webRequest);
        } else {
            try {
                webResponse = this.getWebConnection().getResponse(webRequest);
            } catch (NoHttpResponseException var14) {
                return new WebResponse(responseDataNoHttpResponse_, webRequest, 0L);
            }

            this.getCache().cacheIfPossible(webRequest, (WebResponse) webResponse, (Object) null);
        }

        int status = ((WebResponse) webResponse).getStatusCode();
        if (status == 305) {
            this.getIncorrectnessListener().notify("Ignoring HTTP status code [305] 'Use Proxy'", this);
        } else if (status >= 301 && status <= (this.getBrowserVersion().hasFeature(BrowserVersionFeatures.HTTP_REDIRECT_308) ? 308 : 307) && status != 304 && this.getOptions().isRedirectEnabled()) {
            String locationString = null;

            URL newUrl;
            try {
                locationString = ((WebResponse) webResponse).getResponseHeaderValue("Location");
                if (locationString == null) {
                    return (WebResponse) webResponse;
                }

                if (!this.getBrowserVersion().hasFeature(BrowserVersionFeatures.URL_MINIMAL_QUERY_ENCODING)) {
                    locationString = new String(locationString.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                }

                newUrl = expandUrl(url, locationString);
            } catch (MalformedURLException var15) {
                this.getIncorrectnessListener().notify("Got a redirect status code [" + status + " " + ((WebResponse) webResponse).getStatusMessage() + "] but the location is not a valid URL [" + locationString + "]. Skipping redirection processing.", this);
                return (WebResponse) webResponse;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Got a redirect status code [" + status + "] new location = [" + locationString + "]");
            }

            if (allowedRedirects == 0) {
                throw new FailingHttpStatusCodeException("Too much redirect for " + ((WebResponse) webResponse).getWebRequest().getUrl(), (WebResponse) webResponse);
            }

            WebRequest wrs;
            Iterator var12;
            Entry entry;
            if (status == 301 || status == 302 || status == 303) {
                wrs = new WebRequest(newUrl, HttpMethod.GET);
                wrs.setCharset(webRequest.getCharset());
                if (HttpMethod.HEAD == webRequest.getHttpMethod()) {
                    wrs.setHttpMethod(HttpMethod.HEAD);
                }

                var12 = webRequest.getAdditionalHeaders().entrySet().iterator();

                while (var12.hasNext()) {
                    entry = (Entry) var12.next();
                    wrs.setAdditionalHeader((String) entry.getKey(), (String) entry.getValue());
                }

                return this.loadWebResponseFromWebConnection(wrs, allowedRedirects - 1);
            }

            if (status == 307 || status == 308) {
                wrs = new WebRequest(newUrl, webRequest.getHttpMethod());
                wrs.setCharset(webRequest.getCharset());
                wrs.setRequestParameters(parameters);
                var12 = webRequest.getAdditionalHeaders().entrySet().iterator();

                while (var12.hasNext()) {
                    entry = (Entry) var12.next();
                    wrs.setAdditionalHeader((String) entry.getKey(), (String) entry.getValue());
                }

                return this.loadWebResponseFromWebConnection(wrs, allowedRedirects - 1);
            }
        }


//        cacheResponse.get().put(url.toExternalForm(), (WebResponse) webResponse);

        return (WebResponse) webResponse;
    }

    private void addDefaultHeaders(WebRequest wrs) {
        if (!wrs.isAdditionalHeader("Accept-Language")) {
            wrs.setAdditionalHeader("Accept-Language", this.getBrowserVersion().getBrowserLanguage());
        }

        if (this.getBrowserVersion().hasFeature(BrowserVersionFeatures.HTTP_HEADER_UPGRADE_INSECURE_REQUEST) && !wrs.isAdditionalHeader("Upgrade-Insecure-Requests")) {
            wrs.setAdditionalHeader("Upgrade-Insecure-Requests", "1");
        }

        this.requestHeaders_.forEach((name, value) -> {
            if (!wrs.isAdditionalHeader(name)) {
                wrs.setAdditionalHeader(name, value);
            }

        });
    }

    public List<WebWindow> getWebWindows() {
        return Collections.unmodifiableList(new ArrayList(this.windows_));
    }

    public boolean containsWebWindow(WebWindow webWindow) {
        return this.windows_.contains(webWindow);
    }

    public List<TopLevelWindow> getTopLevelWindows() {
        return Collections.unmodifiableList(new ArrayList(this.topLevelWindows_));
    }

    public void setRefreshHandler(RefreshHandler handler) {
        if (handler == null) {
            this.refreshHandler_ = new NiceRefreshHandler(2);
        } else {
            this.refreshHandler_ = handler;
        }

    }

    public RefreshHandler getRefreshHandler() {
        return this.refreshHandler_;
    }

    public void setScriptPreProcessor(ScriptPreProcessor scriptPreProcessor) {
        this.scriptPreProcessor_ = scriptPreProcessor;
    }

    public ScriptPreProcessor getScriptPreProcessor() {
        return this.scriptPreProcessor_;
    }

    public void setActiveXObjectMap(Map<String, String> activeXObjectMap) {
        this.activeXObjectMap_ = activeXObjectMap;
    }

    public Map<String, String> getActiveXObjectMap() {
        return this.activeXObjectMap_;
    }

    public MSXMLActiveXObjectFactory getMSXMLActiveXObjectFactory() {
        return this.msxmlActiveXObjectFactory_;
    }

    public void setHTMLParserListener(HTMLParserListener listener) {
        this.htmlParserListener_ = listener;
    }

    public HTMLParserListener getHTMLParserListener() {
        return this.htmlParserListener_;
    }

    public CSSErrorHandler getCssErrorHandler() {
        return this.cssErrorHandler_;
    }

    public void setCssErrorHandler(CSSErrorHandler cssErrorHandler) {
        WebAssert.notNull("cssErrorHandler", cssErrorHandler);
        this.cssErrorHandler_ = cssErrorHandler;
    }

    public void setJavaScriptTimeout(long timeout) {
        this.scriptEngine_.setJavaScriptTimeout(timeout);
    }

    public long getJavaScriptTimeout() {
        return this.scriptEngine_.getJavaScriptTimeout();
    }

    public IncorrectnessListener getIncorrectnessListener() {
        return this.incorrectnessListener_;
    }

    public void setIncorrectnessListener(IncorrectnessListener listener) {
        if (listener == null) {
            throw new NullPointerException("Null incorrectness listener.");
        } else {
            this.incorrectnessListener_ = listener;
        }
    }

    public WebConsole getWebConsole() {
        if (this.webConsole_ == null) {
            this.webConsole_ = new WebConsole();
        }

        return this.webConsole_;
    }

    public AjaxController getAjaxController() {
        return this.ajaxController_;
    }

    public void setAjaxController(AjaxController newValue) {
        if (newValue == null) {
            throw new NullPointerException();
        } else {
            this.ajaxController_ = newValue;
        }
    }

    public void setAttachmentHandler(AttachmentHandler handler) {
        this.attachmentHandler_ = handler;
    }

    public AttachmentHandler getAttachmentHandler() {
        return this.attachmentHandler_;
    }

    public void setWebStartHandler(WebStartHandler handler) {
        this.webStartHandler_ = handler;
    }

    public WebStartHandler getWebStartHandler() {
        return this.webStartHandler_;
    }

    public void setAppletConfirmHandler(AppletConfirmHandler handler) {
        this.appletConfirmHandler_ = handler;
    }

    public AppletConfirmHandler getAppletConfirmHandler() {
        return this.appletConfirmHandler_;
    }

    public void setOnbeforeunloadHandler(OnbeforeunloadHandler onbeforeunloadHandler) {
        this.onbeforeunloadHandler_ = onbeforeunloadHandler;
    }

    public OnbeforeunloadHandler getOnbeforeunloadHandler() {
        return this.onbeforeunloadHandler_;
    }

    public Cache getCache() {
        return this.cache_;
    }

    public void setCache(Cache cache) {
        if (cache == null) {
            throw new IllegalArgumentException("cache should not be null!");
        } else {
            this.cache_ = cache;
        }
    }

    public void close() {
        List<TopLevelWindow> topWindows = new ArrayList(this.topLevelWindows_);
        Iterator var2 = topWindows.iterator();

        while (var2.hasNext()) {
            TopLevelWindow topWindow = (TopLevelWindow) var2.next();
            if (this.topLevelWindows_.contains(topWindow)) {
                try {
                    topWindow.close();
                } catch (Exception var7) {
                    LOG.error("Exception while closing a topLevelWindow", var7);
                }
            }
        }

        if (this.scriptEngine_ != null) {
            try {
                this.scriptEngine_.shutdown();
            } catch (Exception var6) {
                LOG.error("Exception while shutdown the scriptEngine", var6);
            }
        }

        try {
            this.webConnection_.close();
        } catch (Exception var5) {
            LOG.error("Exception while closing the connection", var5);
        }

        this.cache_.clear();
    }

    public int waitForBackgroundJavaScript(long timeoutMillis) {
        int count = 0;
        long endTime = System.currentTimeMillis() + timeoutMillis;
        Iterator i = this.jobManagers_.iterator();

        while (true) {
            JavaScriptJobManager jobManager;
            while (true) {
                if (!i.hasNext()) {
                    if (count != this.getAggregateJobCount()) {
                        long newTimeout = endTime - System.currentTimeMillis();
                        return this.waitForBackgroundJavaScript(newTimeout);
                    }

                    return count;
                }

                try {
                    WeakReference<JavaScriptJobManager> reference = (WeakReference) i.next();
                    jobManager = (JavaScriptJobManager) reference.get();
                    if (jobManager != null) {
                        break;
                    }

                    i.remove();
                } catch (ConcurrentModificationException var11) {
                    i = this.jobManagers_.iterator();
                    count = 0;
                }
            }

            long newTimeout = endTime - System.currentTimeMillis();
            count += jobManager.waitForJobs(newTimeout);
        }
    }

    public int waitForBackgroundJavaScriptStartingBefore(long delayMillis) {
        int count = 0;
        long endTime = System.currentTimeMillis() + delayMillis;
        Iterator i = this.jobManagers_.iterator();

        while (true) {
            JavaScriptJobManager jobManager;
            while (true) {
                if (!i.hasNext()) {
                    if (count != this.getAggregateJobCount()) {
                        long newDelay = endTime - System.currentTimeMillis();
                        return this.waitForBackgroundJavaScriptStartingBefore(newDelay);
                    }

                    return count;
                }

                try {
                    WeakReference<JavaScriptJobManager> reference = (WeakReference) i.next();
                    jobManager = (JavaScriptJobManager) reference.get();
                    if (jobManager != null) {
                        break;
                    }

                    i.remove();
                } catch (ConcurrentModificationException var11) {
                    i = this.jobManagers_.iterator();
                    count = 0;
                }
            }

            long newDelay = endTime - System.currentTimeMillis();
            count += jobManager.waitForJobsStartingBefore(newDelay);
        }
    }

    private int getAggregateJobCount() {
        int count = 0;
        Iterator i = this.jobManagers_.iterator();

        while (true) {
            JavaScriptJobManager jobManager;
            while (true) {
                if (!i.hasNext()) {
                    return count;
                }

                try {
                    WeakReference<JavaScriptJobManager> reference = (WeakReference) i.next();
                    jobManager = (JavaScriptJobManager) reference.get();
                    if (jobManager != null) {
                        break;
                    }

                    i.remove();
                } catch (ConcurrentModificationException var6) {
                    i = this.jobManagers_.iterator();
                    count = 0;
                }
            }

            int jobCount = jobManager.getJobCount();
            count += jobCount;
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.webConnection_ = new HttpWebConnection(this);
        this.scriptEngine_ = new JavaScriptEngine(this);
        this.jobManagers_ = Collections.synchronizedList(new ArrayList());
        this.loadQueue_ = new ArrayList();
        if (this.getBrowserVersion().hasFeature(BrowserVersionFeatures.JS_XML_SUPPORT_VIA_ACTIVEXOBJECT)) {
            this.initMSXMLActiveX();
        }

    }

    public void download(WebWindow requestingWindow, String target, WebRequest request, boolean checkHash, boolean forceLoad, String description) {
        WebWindow win = this.resolveWindow(requestingWindow, target);
        URL url = request.getUrl();
        boolean justHashJump = false;
        if (win != null && HttpMethod.POST != request.getHttpMethod()) {
            Page page = win.getEnclosedPage();
            if (page != null) {
                if (page.isHtmlPage() && !((HtmlPage) page).isOnbeforeunloadAccepted()) {
                    return;
                }

                if (checkHash) {
                    URL current = page.getUrl();
                    justHashJump = HttpMethod.GET == request.getHttpMethod() && UrlUtils.sameFile(url, current) && null != url.getRef();
                }
            }
        }

        List var20 = this.loadQueue_;
        synchronized (this.loadQueue_) {
            Iterator var22 = this.loadQueue_.iterator();

            while (var22.hasNext()) {
                WebClient.LoadJob loadJob = (WebClient.LoadJob) var22.next();
                if (loadJob.response_ != null) {
                    WebRequest otherRequest = loadJob.request_;
                    URL otherUrl = otherRequest.getUrl();
                    if (!forceLoad && url.getPath().equals(otherUrl.getPath()) && url.toString().equals(otherUrl.toString()) && request.getRequestParameters().equals(otherRequest.getRequestParameters()) && StringUtils.equals(request.getRequestBody(), otherRequest.getRequestBody())) {
                        return;
                    }
                }
            }
        }

        WebClient.LoadJob loadJob;
        if (justHashJump) {
            loadJob = new WebClient.LoadJob(request, requestingWindow, target, url);
        } else {
            try {
                WebResponse response = this.loadWebResponse(request);
                loadJob = new WebClient.LoadJob(request, requestingWindow, target, response);
            } catch (IOException var18) {
                throw new RuntimeException(var18);
            }
        }

        List var24 = this.loadQueue_;
        synchronized (this.loadQueue_) {
            this.loadQueue_.add(loadJob);
        }
    }

    public void loadDownloadedResponses() throws FailingHttpStatusCodeException, IOException {
        List var2 = this.loadQueue_;
        ArrayList queue;
        synchronized (this.loadQueue_) {
            if (this.loadQueue_.isEmpty()) {
                return;
            }

            queue = new ArrayList(this.loadQueue_);
            this.loadQueue_.clear();
        }

        HashSet<WebWindow> updatedWindows = new HashSet();

        for (int i = queue.size() - 1; i >= 0; --i) {
            WebClient.LoadJob loadJob = (WebClient.LoadJob) queue.get(i);
            if (loadJob.isOutdated()) {
                LOG.info("No usage of download: " + loadJob);
            } else {
                WebWindow window = this.resolveWindow(loadJob.requestingWindow_, loadJob.target_);
                if (!updatedWindows.contains(window)) {
                    WebWindow win = this.openTargetWindow(loadJob.requestingWindow_, loadJob.target_, "_self");
                    if (loadJob.urlWithOnlyHashChange_ != null) {
                        HtmlPage page = (HtmlPage) loadJob.requestingWindow_.getEnclosedPage();
                        String oldURL = page.getUrl().toExternalForm();
                        WebRequest req = page.getWebResponse().getWebRequest();
                        req.setUrl(loadJob.urlWithOnlyHashChange_);
                        Window jsWindow = (Window) win.getScriptableObject();
                        if (null != jsWindow) {
                            Location location = jsWindow.getLocation();
                            location.setHash(oldURL, loadJob.urlWithOnlyHashChange_.getRef());
                        }

                        win.getHistory().addPage(page);
                    } else {
                        Page pageBeforeLoad = win.getEnclosedPage();
                        this.loadWebResponseInto(loadJob.response_, win);
                        if (this.scriptEngine_ != null) {
                            this.scriptEngine_.registerWindowAndMaybeStartEventLoop(win);
                        }

                        if (pageBeforeLoad != win.getEnclosedPage()) {
                            updatedWindows.add(win);
                        }

                        this.throwFailingHttpStatusCodeExceptionIfNecessary(loadJob.response_);
                    }
                } else {
                    LOG.info("No usage of download: " + loadJob);
                }
            }
        }

    }

    public WebClientOptions getOptions() {
        return this.options_;
    }

    public WebClientInternals getInternals() {
        return this.internals_;
    }

    public StorageHolder getStorageHolder() {
        return this.storageHolder_;
    }

    public synchronized Set<Cookie> getCookies(URL url) {
        CookieManager cookieManager = this.getCookieManager();
        if (!cookieManager.isCookiesEnabled()) {
            return Collections.emptySet();
        } else {
            URL normalizedUrl = cookieManager.replaceForCookieIfNecessary(url);
            String host = normalizedUrl.getHost();
            if (host.isEmpty()) {
                return Collections.emptySet();
            } else {
                String path = normalizedUrl.getPath();
                String protocol = normalizedUrl.getProtocol();
                boolean secure = "https".equals(protocol);
                int port = cookieManager.getPort(normalizedUrl);
                cookieManager.clearExpired(new Date());
                List<org.apache.http.cookie.Cookie> all = Cookie.toHttpClient(cookieManager.getCookies());
                List<org.apache.http.cookie.Cookie> matches = new ArrayList();
                if (all.size() > 0) {
                    CookieOrigin cookieOrigin = new CookieOrigin(host, port, path, secure);
                    CookieSpec cookieSpec = new HtmlUnitBrowserCompatCookieSpec(this.getBrowserVersion());
                    Iterator var13 = all.iterator();

                    while (var13.hasNext()) {
                        org.apache.http.cookie.Cookie cookie = (org.apache.http.cookie.Cookie) var13.next();
                        if (cookieSpec.match(cookie, cookieOrigin)) {
                            matches.add(cookie);
                        }
                    }
                }

                Set<Cookie> cookies = new LinkedHashSet();
                cookies.addAll(Cookie.fromHttpClient(matches));
                return Collections.unmodifiableSet(cookies);
            }
        }
    }

    public void addCookie(String cookieString, URL pageUrl, Object origin) {
        BrowserVersion browserVersion = this.getBrowserVersion();
        CookieManager cookieManager = this.getCookieManager();
        if (cookieManager.isCookiesEnabled()) {
            CharArrayBuffer buffer = new CharArrayBuffer(cookieString.length() + 22);
            buffer.append("Set-Cookie: ");
            buffer.append(cookieString);
            HtmlUnitBrowserCompatCookieSpec cookieSpec = new HtmlUnitBrowserCompatCookieSpec(browserVersion);

            try {
                List<org.apache.http.cookie.Cookie> cookies = cookieSpec.parse(new BufferedHeader(buffer), cookieManager.buildCookieOrigin(pageUrl));
                Iterator var9 = cookies.iterator();

                while (var9.hasNext()) {
                    org.apache.http.cookie.Cookie cookie = (org.apache.http.cookie.Cookie) var9.next();
                    Cookie htmlUnitCookie = new Cookie((ClientCookie) cookie);
                    cookieManager.addCookie(htmlUnitCookie);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Added cookie: '" + cookieString + "'");
                    }
                }
            } catch (MalformedCookieException var12) {
                this.getIncorrectnessListener().notify("set-cookie http-equiv meta tag: invalid cookie '" + cookieString + "'; reason: '" + var12.getMessage() + "'.", origin);
            }
        } else if (LOG.isDebugEnabled()) {
            LOG.debug("Skipped adding cookie: '" + cookieString + "'");
        }

    }

    private static class LoadJob {
        private final WebWindow requestingWindow_;
        private final String target_;
        private final WebResponse response_;
        private final URL urlWithOnlyHashChange_;
        private final WeakReference<Page> originalPage_;
        private final WebRequest request_;

        LoadJob(WebRequest request, WebWindow requestingWindow, String target, WebResponse response) {
            this.request_ = request;
            this.requestingWindow_ = requestingWindow;
            this.target_ = target;
            this.response_ = response;
            this.urlWithOnlyHashChange_ = null;
            this.originalPage_ = new WeakReference(requestingWindow.getEnclosedPage());
        }

        LoadJob(WebRequest request, WebWindow requestingWindow, String target, URL urlWithOnlyHashChange) {
            this.request_ = request;
            this.requestingWindow_ = requestingWindow;
            this.target_ = target;
            this.response_ = null;
            this.urlWithOnlyHashChange_ = urlWithOnlyHashChange;
            this.originalPage_ = new WeakReference(requestingWindow.getEnclosedPage());
        }

        public boolean isOutdated() {
            if (this.target_ != null && !this.target_.isEmpty()) {
                return false;
            } else if (this.requestingWindow_.isClosed()) {
                return true;
            } else {
                return this.requestingWindow_.getEnclosedPage() != this.originalPage_.get();
            }
        }
    }

    private static final class CurrentWindowTracker implements WebWindowListener, Serializable {
        private final WebClient webClient_;

        private CurrentWindowTracker(WebClient webClient) {
            this.webClient_ = webClient;
        }

        public void webWindowClosed(WebWindowEvent event) {
            WebWindow window = event.getWebWindow();
            if (window instanceof TopLevelWindow) {
                this.webClient_.topLevelWindows_.remove(window);
                if (window == this.webClient_.getCurrentWindow()) {
                    if (this.webClient_.topLevelWindows_.isEmpty()) {
                        TopLevelWindow newWindow = new TopLevelWindow("", this.webClient_);
                        this.webClient_.topLevelWindows_.push(newWindow);
                        this.webClient_.setCurrentWindow(newWindow);
                    } else {
                        this.webClient_.setCurrentWindow((WebWindow) this.webClient_.topLevelWindows_.peek());
                    }
                }
            } else if (window == this.webClient_.getCurrentWindow()) {
                this.webClient_.setCurrentWindow((WebWindow) this.webClient_.topLevelWindows_.peek());
            }

        }

        public void webWindowContentChanged(WebWindowEvent event) {
            WebWindow window = event.getWebWindow();
            boolean use = false;
            if (window instanceof DialogWindow) {
                use = true;
            } else if (window instanceof TopLevelWindow) {
                use = event.getOldPage() == null;
            } else if (window instanceof FrameWindow) {
                FrameWindow fw = (FrameWindow) window;
                String enclosingPageState = fw.getEnclosingPage().getDocumentElement().getReadyState();
                URL frameUrl = fw.getEnclosedPage().getUrl();
                if (!"complete".equals(enclosingPageState) || frameUrl == WebClient.URL_ABOUT_BLANK) {
                    return;
                }

                BaseFrameElement frameElement = fw.getFrameElement();
                if (frameElement.isDisplayed()) {
                    Object element = frameElement.getScriptableObject();
                    HTMLElement htmlElement = (HTMLElement) element;
                    ComputedCSSStyleDeclaration style = htmlElement.getWindow().getComputedStyle(htmlElement, (String) null);
                    use = style.getCalculatedWidth(false, false) != 0 && style.getCalculatedHeight(false, false) != 0;
                }
            }

            if (use) {
                this.webClient_.setCurrentWindow(window);
            }

        }

        public void webWindowOpened(WebWindowEvent event) {
            WebWindow window = event.getWebWindow();
            if (window instanceof TopLevelWindow) {
                TopLevelWindow tlw = (TopLevelWindow) window;
                this.webClient_.topLevelWindows_.push(tlw);
            }

        }
    }
}
