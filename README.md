i18n-checker
============
A Java implementation of the W3C Internationalization Checker (http://validator.w3.org/i18n-checker/). Provides an API for running internationalization (i18n) checks on a document.

**Notice:** This project is in the early stages of its development; there may be
design changes and some classes are undocumented.

Usage
-----
To perform an i18n check, import [`org.w3.i18n.I18nChecker`](http://github.com/w3c/i18n-checker/blob/master/src/main/java/org/w3/i18n/I18nChecker.java) and use either:
* `static List<Assertion> check(URL url)`, for online checks; or
* `static List<Assertion> check(URL url, InputStream body, Map<String,List<String>> headers)`, for offline checks.

The results are provided as a list of  [`org.w3.i18n.Assertion`](https://github.com/w3c/i18n-checker/blob/master/src/main/java/org/w3/i18n/Assertion.java). An Assertion has an ID, level, HTML title, HTML description, and a list of contexts. Together these items say something thing about the document. For example, an Assertion could have:
```
id = charset_http
level = INFO
htmlTitle = Charset declaration in HTTP response header 'Content-Type'
htmlDescription = …
contexts = [utf-8, Content-Type: text/html; charset=utf-8]
```

Assertions can be `INFO`, `WARNING`, `ERROR`, or `MESSAGE` level. `MESSAGE` level Assertions contain meta information about the operation of the checker. `WARNING` and `ERROR` level assertions describe i18n problems with the document. In such cases the `htmlTitle` and `htmlDescription` provide advice on solving the problem.

A list of Assertions could be further formatted and displayed to your user. Here's a simple example:
```java
/* Runs the i18n-checker on the given URL and prints the results to the
 * standard output stream. */
private static void printI18nCheck(java.net.URL url)
        throws java.io.IOException {
    java.util.List<org.w3.i18n.Assertion> results =
            org.w3.i18n.I18nChecker.check(url);
    for (Assertion assertion : results) {
        System.out.printf(
            "%s\n\t%s\n", assertion.getId(), assertion.getContexts());
    }
}
```

The output from calling this method would be something like:
```
charset_http
   [utf-8, Content-Type: text/html; charset=utf-8]
charset_meta
   [utf-8, <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />]
lang_attr_lang
   [en, <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">]
mimetype
   [text/html; charset=utf-8]
…
```
