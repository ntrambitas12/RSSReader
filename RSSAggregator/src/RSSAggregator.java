import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.xmltree.XMLTree;
import components.xmltree.XMLTree1;

/**
 * Program to convert an XML RSS (version 2.0) feed from a given URL into the
 * corresponding HTML output file.
 *
 * @author Nicholas Trambitas
 *
 */
public final class RSSAggregator {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private RSSAggregator() {
    }

    /**
     * Outputs the "opening" tags in the generated HTML file. These are the
     * expected elements generated by this method:
     *
     * <html> <head> <title>the channel tag title as the page title</title>
     * </head> <body>
     * <h1>the page title inside a link to the <channel> link</h1>
     * <p>
     * the channel description
     * </p>
     * <table border="1">
     * <tr>
     * <th>Date</th>
     * <th>Source</th>
     * <th>News</th>
     * </tr>
     *
     * @param channel
     *            the channel element XMLTree
     * @param out
     *            the output stream
     * @updates out.content
     * @requires [the root of channel is a <channel> tag] and out.is_open
     * @ensures out.content = #out.content * [the HTML "opening" tags]
     */
    private static void outputHeader(XMLTree channel, SimpleWriter out) {
        assert channel != null : "Violation of: channel is not null";
        assert out != null : "Violation of: out is not null";
        assert channel.isTag() && channel.label().equals("channel") : ""
                + "Violation of: the label root of channel is a <channel> tag";
        assert out.isOpen() : "Violation of: out.is_open";

        int title = getChildElement(channel, "title");
        int link = getChildElement(channel, "link");
        int description = getChildElement(channel, "description");

        //print basic html tags to file
        out.println("<html>");
        out.println("<head>");

        //set title of page
        if (channel.child(title).numberOfChildren() > 0) {
            out.println("<title>" + channel.child(title).child(0).label()
                    + "</title>");
        } else {
            out.println("<title>Empty Title</title>");
        }

        out.println("</head>");
        out.println("<body>");
        out.println("<h1>");
        //prints out heading
        if (channel.child(title).numberOfChildren() > 0) {
            if (channel.child(link).numberOfChildren() > 0) {
                out.println("<a href=\"" + channel.child(link).child(0).label()
                        + "\">");
                out.println(channel.child(title).child(0).label() + "</a>");

                //if there is no link

            } else {
                out.println(channel.child(title).child(0).label());

            }

            //if title is empty:
        } else {
            if (channel.child(link).numberOfChildren() > 0) {
                out.println("<a href=\"" + channel.child(link).child(0).label()
                        + "\">");
                out.println("Empty Title" + "</a>");

                //if no link
            } else {
                out.println("Empty Title, no Link");
            }
        }

        out.println("</h1>");

        //print out channel description
        out.println("<p>");
        if (channel.child(description).numberOfChildren() > 0) {
            out.println(channel.child(description).child(0).label());

            //if description is empty:
        } else {
            out.println("No description");
        }
        out.println("</p>");

        //set the table up
        out.println("<table border=\"1\">");
        out.println("<tr>");
        out.println("<th>Date</th>");
        out.println("<th>Source</th>");
        out.println("<th>News</th>");
        out.println("</tr>");

    }

    /**
     * Outputs the "closing" tags in the generated HTML file. These are the
     * expected elements generated by this method:
     *
     * </table>
     * </body> </html>
     *
     * @param out
     *            the output stream
     * @updates out.contents
     * @requires out.is_open
     * @ensures out.content = #out.content * [the HTML "closing" tags]
     */
    private static void outputFooter(SimpleWriter out) {
        assert out != null : "Violation of: out is not null";
        assert out.isOpen() : "Violation of: out.is_open";

        out.println("</table>");
        out.println("</body>");
        out.println("</html>");
    }

    /**
     * Finds the first occurrence of the given tag among the children of the
     * given {@code XMLTree} and return its index; returns -1 if not found.
     *
     * @param xml
     *            the {@code XMLTree} to search
     * @param tag
     *            the tag to look for
     * @return the index of the first child of type tag of the {@code XMLTree}
     *         or -1 if not found
     * @requires [the label of the root of xml is a tag]
     * @ensures <pre>
     * getChildElement =
     *  [the index of the first child of type tag of the {@code XMLTree} or
     *   -1 if not found]
     * </pre>
     */
    private static int getChildElement(XMLTree xml, String tag) {
        assert xml != null : "Violation of: xml is not null";
        assert tag != null : "Violation of: tag is not null";
        assert xml.isTag() : "Violation of: the label root of xml is a tag";

        int index = -1;
        boolean reached = false;
        for (int i = 0; (i < xml.numberOfChildren()) && (!reached); i++) {
            if (xml.child(i).label().equals(tag)) {
                index = i;
                reached = true;
            }
        }
        return index;
    }

    /**
     * Processes one news item and outputs one table row. The row contains three
     * elements: the publication date, the source, and the title (or
     * description) of the item.
     *
     * @param item
     *            the news item
     * @param out
     *            the output stream
     * @updates out.content
     * @requires [the label of the root of item is an <item> tag] and
     *           out.is_open
     * @ensures <pre>
     * out.content = #out.content *
     *   [an HTML table row with publication date, source, and title of news item]
     * </pre>
     */
    private static void processItem(XMLTree item, SimpleWriter out) {
        assert item != null : "Violation of: item is not null";
        assert out != null : "Violation of: out is not null";
        assert item.isTag() && item.label().equals("item") : ""
                + "Violation of: the label root of item is an <item> tag";
        assert out.isOpen() : "Violation of: out.is_open";

        int pub = getChildElement(item, "pubDate");
        int source = getChildElement(item, "source");
        int description = getChildElement(item, "description");
        int link = getChildElement(item, "link");
        int title = getChildElement(item, "title");
        String valueOfPub = "";
        String valueOfSource = "";
        String valueOfTitle = "";

        //determine value of publication
        if (pub == -1) {
            valueOfPub = "No date available";
        } else {
            valueOfPub = item.child(pub).child(0).label();
        }

        //determine value of source
        if (source == -1) {
            valueOfSource = "No source available";

        }

        if ((source != -1) && (item.child(source).numberOfChildren() > 0)) {
            valueOfSource = "<a href =\""
                    + item.child(source).attributeValue("url") + "\">"
                    + item.child(source).child(0).label() + "</a>";
        }

        if ((source != -1) && (item.child(source).numberOfChildren() == 0)) {
            valueOfSource = "<a href =\""
                    + item.child(source).attributeValue("url") + "\">"
                    + "No child source tag provided" + "</a>";
        }

        //determine value of title

        if (link != -1) {
            if (item.child(link).numberOfChildren() > 0) {

                valueOfTitle = "<a href =\"" + item.child(link).child(0).label()
                        + "\">" + "No title available" + "</a>";

            } else {
                valueOfTitle = "No title available";
            }

        }

        if (description != -1) {
            if (item.child(description).numberOfChildren() > 0) {
                valueOfTitle = item.child(description).child(0).label();
                if (link != -1) {
                    if (item.child(link).numberOfChildren() > 0) {

                        valueOfTitle = "<a href =\""
                                + item.child(link).child(0).label() + "\">"
                                + item.child(description).child(0).label()
                                + "</a>";
                    }
                }
            }

        }

        if (title != -1) {
            if (item.child(title).numberOfChildren() > 0) {
                valueOfTitle = item.child(title).child(0).label();
                if (link != -1) {
                    if (item.child(link).numberOfChildren() > 0) {

                        valueOfTitle = "<a href =\""
                                + item.child(link).child(0).label() + "\">"
                                + item.child(title).child(0).label() + "</a>";
                    }
                }

            }

        }

        //print table data to html
        out.println("<tr>");

        out.println("   <td>" + valueOfPub + "</td>");
        out.println("   <td>" + valueOfSource + "</td>");
        out.println("   <td>" + valueOfTitle + "</td>");
        out.println("</tr>");

    }

    /**
     * Processes one XML RSS (version 2.0) feed from a given URL converting it
     * into the corresponding HTML output file.
     *
     * @param url
     *            the URL of the RSS feed
     * @param file
     *            the name of the HTML output file
     * @param out1
     *            the output stream to report progress or errors
     * @updates out.content
     * @requires out.is_open
     * @ensures <pre>
     * [reads RSS feed from url, saves HTML document with table of news items
     *   to file, appends to out.content any needed messages]
     * </pre>
     */
    private static void processFeed(String url, String file,
            SimpleWriter out1) {
        SimpleWriter out = new SimpleWriter1L(file);
        XMLTree xml = new XMLTree1(url);

        //Check to make sure link is actual RSS 2.0 feed

        if ((xml.isTag()) && (xml.label().equals("rss"))) {
            if (xml.hasAttribute("version")) {
                if (xml.attributeValue("version").equals("2.0")) {

                    //print heading
                    XMLTree channel = xml.child(0);
                    outputHeader(channel, out);

                    //print all news sources to html page
                    for (int i = 0; i < channel.numberOfChildren(); i++) {
                        XMLTree child = channel.child(i);
                        if (child.label().equals("item")) {
                            processItem(child, out);
                        }
                    }

                    //print end of html
                    outputFooter(out);

                }
            }
        }

        //close output stream and print to user result
        out.close();
        out1.println("Successfully generated " + file);
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        //output to html
        SimpleWriter out = new SimpleWriter1L("index.html");
        //output to cmd window
        SimpleWriter out1 = new SimpleWriter1L();

        out1.println("Enter the name of the XML file to aggregate from: ");
        String url = in.nextLine();

        XMLTree aggregator = new XMLTree1(url);

        //print basic html tags to file
        out.println("<html>");
        out.println("<head>");
        out.println("<title>");
        out.println(aggregator.attributeValue("title"));
        out.println("</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>");
        out.println(aggregator.attributeValue("title"));
        out.println("</h1>");
        out.println("<ul>");
        int numChildren = aggregator.numberOfChildren();

        for (int i = 0; i < numChildren; i++) {
            //make sure the tag is a feed tag
            if (aggregator.child(i).label().equals("feed")) {
                processFeed(aggregator.child(i).attributeValue("url"),
                        aggregator.child(i).attributeValue("file"), out1);

                //prints out the list of articles on index page
                out.println("<li>");
                out.println("<a href=\""
                        + aggregator.child(i).attributeValue("file") + "\">");
                out.println(aggregator.child(i).attributeValue("name"));
                out.println("</a>");
                out.println("</li>");
            }
        }

        //close/finish index.html
        out.println("</ul>");
        out.println("</body>");
        out.println("</html>");

        //close output streams
        in.close();
        out.close();
        out1.close();
    }

}
