# lein-nitpicker

A Leiningen plugin that enforces Lupapiste coding style:
  * tabulator, carriage return and non-ascii characters are not allowed
    (for cross platform development compatibility)
  * JavaScript files must not contain console.log calls
    (which would break Internet Explorer).

## Usage

[![Clojars Project](http://clojars.org/lupapiste/lein-nitpicker/latest-version.svg)](http://clojars.org/lupapiste/lein-nitpicker)

Put `[lupapiste/lein-nitpicker "0.5.1"]` into the `:plugins` vector
of your project.clj and run:

    lein nitpicker

## Configuration

Put a map under `:nitpicker` key in your project.clj. Supported keys:
 * `:sources` - a collection of directories to be checked
 * `:exts` - a collection of file extensions of files to be checked
 * `:excludes` - a collection of regular expressions of file names/paths to ignore

The default configuration is:

```clojure
(defproject my-project

  :nitpicker {
    :sources (concat (:source-paths project) (:resource-paths project))
    :exts ["clj" "js" "css" "html"]
    :excludes [#"\/jquery\/" #"\/theme\/default\/" #"\/public\/lib\/"]
  }
)
```

## License

Copyright © 2012-2015 Solita Oy

Distributed under the Eclipse Public License, the same as Clojure.
