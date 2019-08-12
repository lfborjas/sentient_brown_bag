# Sentient Brown Bag

## Setup

This little project uses Clojure. Go [here](https://clojure.org/guides/getting_started)
to get started; and [over here](https://clojure.org/guides/deps_and_cli) to get more familiar with the command line
tools (e.g. `clj`).

Optionally, I use [CIDER]() for Clojure development, and [Org Mode]() to write
the slides (with [`org-present`](https://github.com/rlister/org-present) and `org-reveal` to present from Emacs and from
the browser, respectively.)

## Running

### Running the code from the command line

You should be able to either spin a Clojure repl with `clj`, import the `sbb`
namespace and play around, or you can execute the sample code with `clj -m sbb`.


### Running the slides

To run the presentation from emacs, go to `presentation.org` and run `M-x org-present`. You can navigate with left and right, and quit with `C-c C-q`:
* To embiggen the text, do `C-c C-=`
* To run inline code, make sure `CIDER` is running (or run `M-x cider-jack-in`) and do `C-c C-e` to evaluate inline
* To evaluate code, make sure the code block has `:results output` as the option, and run `C-c C-c` -- the output will be added in a `RESULTS` section
below the code.

For more code interaction tips, check out the [Babel](https://orgmode.org/worg/org-contrib/babel/intro.html#results) documentation.

To create a JS presentation off the org file, use: 
