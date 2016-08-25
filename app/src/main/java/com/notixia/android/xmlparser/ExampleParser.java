package com.notixia.android.xmlparser;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rsn on 24/08/16.
 */
public class ExampleParser {
    // We don't use a namespace
    private static final String ns = null;

    public List parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFootballClubs(parser);
        } finally {
            in.close();
        }
    }

    private List readFootballClubs(XmlPullParser parser) throws XmlPullParserException, IOException {
        List clubs = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "football_clubs");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the 'Club' tag
            if (name.equals("club")) {
                clubs.add(readClub(parser));
            } else {
                skip(parser);
            }
        }
        return clubs;
    }

    public static class Club {
        public final String name;
        public final String link;

        private Club(String name, String link) {
            this.name = name;
            this.link = link;
        }
    }

    // Parses the contents of an 'Club'. If it encounters a name or link tag, hands them
    // to their respective "read" methods for processing. Otherwise, skips the tag.
    private Club readClub(XmlPullParser parser) throws  XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "club");
        String name = null;
        String link = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String parserName = parser.getName();
            if (parserName.equals("name")) {
                name = readName(parser);
            } else if (name.equals("link")) {
                link = readLink(parser);
            } else {
                skip(parser);
            }
        }
        return new Club(name, link);
    }

    // Processes name tags in the club tag.
    private String readName(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "name");
        String name = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "name");
        return name;
    }

    // Process link tags in the club tag.
    private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
        String link = "";
        parser.require(XmlPullParser.START_TAG, ns, "link");
        String tag = parser.getName();
        String relType = parser.getAttributeValue(null, "rel");
        if (tag.equals("link")) {
            if (relType.equals("alternate")) {
                link = parser.getAttributeValue(null, "href");
                parser.nextTag();
            }
        }
        parser.require(XmlPullParser.END_TAG, ns, "link");
        return link;
    }

    // For the tag name, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.next();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
             throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

}
