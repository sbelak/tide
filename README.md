# tide

A Clojure library for time series analysis.

Currently it implements the following algorithms:

* [STL: A Seasonal-Trend Decomposition Procedure Based on Loess](http://www.wessa.net/download/stl.pdf) via [stl-decomp-4j](https://github.com/ServiceNow/stl-decomp-4j)
* [FastDTW: Toward Accurate Dynamic Time Warping in Linear Time and Space](http://cs.fit.edu/~pkc/papers/tdm04.pdf) via [fastdtw](https://github.com/davidmoten/fastdtw)

## Usage

Add this dependency to your project:

```clj
[tide "0.2.0-SNAPSHOT"]
```

## [Examples](http://viewer.gorilla-repl.org/view.html?source=github&user=sbelak&repo=tide&path=examples/examples.cljw)


## License

Copyright © 2017 Simon Belak

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
