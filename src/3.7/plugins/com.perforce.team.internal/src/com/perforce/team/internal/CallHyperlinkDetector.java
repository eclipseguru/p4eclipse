/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.internal;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.ui.BaseHyperlinkDetector;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.browser.WebBrowserPreference;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class CallHyperlinkDetector extends BaseHyperlinkDetector {

    /**
     * CALL_URL
     */
    public static final String CALL_URL = "http://calltrack.perforce.com/cgi-bin/detail?call=";

    /**
     * CALL_REGEX
     */
    public static final String CALL_REGEX = "(^|((^|\\s)(call|CALL)?( )?#?))(\\d\\d\\d\\d\\d+)($|\\s)";

    private Pattern callRegex = null;

    /**
     * Create a call number hyperlink detector
     */
    public CallHyperlinkDetector() {
        this.callRegex = Pattern.compile(CALL_REGEX, Pattern.MULTILINE);
    }

    private IHyperlink createHyperlink(final IRegion region, String id) {
        if (id != null) {
            try {
                final int clId = Integer.parseInt(id);
                if (clId > 0) {
                    return new IHyperlink() {

                        public void open() {
                            openCall(clId);
                        }

                        public String getTypeLabel() {
                            return null;
                        }

                        public String getHyperlinkText() {
                            return "View call " + clId;
                        }

                        public IRegion getHyperlinkRegion() {
                            return region;
                        }
                    };
                }
            } catch (NumberFormatException nfe) {
                return null;
            }

        }
        return null;
    }

    private void openCall(int id) {
        IWorkbenchBrowserSupport support = PlatformUI.getWorkbench()
                .getBrowserSupport();
        if (support != null) {
            try {
                URL url = new URL(CALL_URL + id);
                IWebBrowser browser = null;
                if (WebBrowserPreference.getBrowserChoice() == WebBrowserPreference.INTERNAL
                        && support.isInternalWebBrowserAvailable()) {
                    browser = support.createBrowser(
                            IWorkbenchBrowserSupport.AS_EDITOR
                                    | IWorkbenchBrowserSupport.LOCATION_BAR
                                    | IWorkbenchBrowserSupport.NAVIGATION_BAR,
                            "call" + id, null, null);
                } else {
                    browser = support.getExternalBrowser();
                }
                if (browser != null) {
                    browser.openURL(url);
                }
            } catch (PartInitException e) {
                PerforceProviderPlugin.logError(e);
            } catch (MalformedURLException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
    }

    /**
     * @see com.perforce.team.ui.BaseHyperlinkDetector#generateHyperlinks(java.lang.String,
     *      int, int)
     */
    @Override
    protected Collection<IHyperlink> generateHyperlinks(String value,
            int regionOffset, int positionOffset) {
        Collection<IHyperlink> links = null;
        Matcher matcher = this.callRegex.matcher(value);
        while (matcher.find()) {
            int length = matcher.group().length();
            int start = matcher.start();
            String call = matcher.group(0);
            if (call.startsWith(" ")) {
                start++;
                length--;
            }
            if (call.endsWith(" ")) {
                length--;
            }
            if (accept(positionOffset, start, length)) {
                start += regionOffset;
                IRegion match = new Region(start, length);
                String id = matcher.group(6);
                if (id != null) {
                    IHyperlink link = createHyperlink(match, id);
                    if (link != null) {
                        if (links == null) {
                            links = new ArrayList<IHyperlink>();
                        }
                        links.add(link);
                    }
                }
            }
        }
        return links;
    }
}
