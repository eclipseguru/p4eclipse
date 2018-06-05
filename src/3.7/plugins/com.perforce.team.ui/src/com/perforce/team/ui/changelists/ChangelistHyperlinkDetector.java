/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.changelists;

import com.perforce.p4java.core.IChangelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.BaseHyperlinkDetector;
import com.perforce.team.ui.p4java.actions.ViewChangelistAction;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ChangelistHyperlinkDetector extends BaseHyperlinkDetector {

    /**
     * CHANGELIST_REGEX
     */
    public static final String CHANGELIST_REGEX = "(^|\\W)(c|C)hange(list)?( )?#?(\\d+)";

    private Pattern changelistRegex = null;

    /**
     * Create a changelist hyperlink detector
     */
    public ChangelistHyperlinkDetector() {
        this.changelistRegex = Pattern.compile(CHANGELIST_REGEX);
    }

    private IHyperlink createHyperlink(final IRegion region,
            final IP4Connection connection, String id) {
        if (connection != null) {
            try {
                final int clId = Integer.parseInt(id);
                if (clId > IChangelist.DEFAULT) {
                    return new IHyperlink() {

                        public void open() {
                            openChangelist(clId, connection);
                        }

                        public String getTypeLabel() {
                            return null;
                        }

                        public String getHyperlinkText() {
                            return MessageFormat
                                    .format(Messages.ChangelistHyperlinkDetector_ViewChangelist,
                                            clId);
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

    private void openChangelist(int id, IP4Connection connection) {
        ViewChangelistAction view = new ViewChangelistAction();
        view.view(id, connection);
    }

    /**
     * @see com.perforce.team.ui.BaseHyperlinkDetector#generateHyperlinks(java.lang.String,
     *      int, int)
     */
    @Override
    protected Collection<IHyperlink> generateHyperlinks(String value,
            int regionOffset, int positionOffset) {
        Matcher matcher = this.changelistRegex.matcher(value);
        List<IHyperlink> links = null;
        while (matcher.find()) {
            int length = matcher.group().length();
            int start = matcher.start();
            String prefix = matcher.group(1);
            if (prefix != null && prefix.length() > 0) {
                start++;
                length--;
            }
            if (accept(positionOffset, start, length)) {
                start += regionOffset;
                String id = matcher.group(5);
                if (id != null) {
                    IRegion match = new Region(start, length);
                    IP4Connection connection = getConnection();
                    IHyperlink link = createHyperlink(match, connection, id);
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
