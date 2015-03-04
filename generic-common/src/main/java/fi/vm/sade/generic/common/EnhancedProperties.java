package fi.vm.sade.generic.common;

import java.io.*;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enhanced Properties, that will handle 'nested properties' (using property values inside properties), for example:
 * <p/>
 * foo=bar
 * <br/>
 * asd=value of foo-property here: ${foo}
 */
public class EnhancedProperties extends Properties {

    private static EnhancedProperties commonAndSystemProperties;

    public EnhancedProperties() {
    }

    public static EnhancedProperties getCommonAndSystemProperties() {
        if (commonAndSystemProperties == null) {
            try {
                commonAndSystemProperties = new EnhancedProperties(System.getProperties());
                commonAndSystemProperties.load(new FileInputStream(new File(System.getProperty("user.home") + "/oph-configuration/common.properties")));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return commonAndSystemProperties;
    }

    public EnhancedProperties(Properties defaults) {
        super(defaults);
        processSubstitutions();
    }

    @Override
    public synchronized void load(Reader reader) throws IOException {
        super.load(reader);
        processSubstitutions();
    }

    @Override
    public synchronized void load(InputStream inStream) throws IOException {
        super.load(inStream);
        processSubstitutions();
    }

    @Override
    public synchronized void loadFromXML(InputStream in) throws IOException, InvalidPropertiesFormatException {
        super.loadFromXML(in);
        processSubstitutions();
    }

    private void processSubstitutions() {
        // prepare replacements
        Map<String, String> replacements = new HashMap<String, String>();
        for (String key : this.stringPropertyNames()) {
            replacements.put("${" + key + "}", this.getProperty(key));
        }

        // process replacements for all properties
        boolean recursionNeeded = false;
        for (String key : this.stringPropertyNames()) {
            String value = this.getProperty(key);
            String newValue = substituteValue(value, replacements);
            if (newValue == null) {
                recursionNeeded = true;
                continue;
            }
            if (!value.equals(newValue)) {
                this.put(key, newValue);
//                System.out.println("property substituted: "+value+" -> "+newValue);
            }
        }

        // recurse if needed
        if (recursionNeeded) {
            processSubstitutions();
        }
    }

    /** @return substituted value OR null if we need recursion */
    private String substituteValue(String value, Map<String, String> replacements) {
        String rx = "(\\$\\{[^}]+\\})";
        StringBuffer sb = new StringBuffer();
        Pattern p = Pattern.compile(rx);
        Matcher m = p.matcher(value);
        while (m.find()) {
            String group = m.group(1);
            String replacement = replacements.get(group);
            if (replacement != null) {
                if (replacement.contains("${")) {
                    return null; // needs recursion because should be substituted with value that is not yet substituted itself!
                } else {
                    try {
                        m.appendReplacement(sb, replacement);
                    } catch (Exception e) {
                        throw new RuntimeException("cannot replace, original: "+value+", replacement: "+replacement+", group: "+group+", exception: "+e);
                    }
                }
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
