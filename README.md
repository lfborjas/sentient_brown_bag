# Sentient Brown Bag

A small 1-hour exploration of logic programming, using Clojure's `core.logic` contrib library as inspiration and tool, and following the examples set forth in The Joy of Clojure's delightful chapter on the matter.

If you're looking for the slides, Github happens to have pretty neat rendering of org-mode files so look no further than [presentation.org](https://github.com/lfborjas/sentient_brown_bag/blob/master/presentation.org)

## Setup

This little project uses Clojure. Go [here](https://clojure.org/guides/getting_started)
to get started; and [over here](https://clojure.org/guides/deps_and_cli) to get more familiar with the command line
tools (e.g. `clj`).

Optionally, I use [CIDER](https://cider.readthedocs.io/en/stable/) for Clojure development, and [Org Mode](https://orgmode.org/) to write
the slides (with [`org-present`](https://github.com/rlister/org-present) to present from Emacs)


## Running

### Running the code from the command line

You should be able to either spin a Clojure repl with `clj`, import from the `sentient-brown-bag`
namespace and play around. The source files have `(comment ...)` blocks with little snippets you should be able to throw at a REPL and explore. I personally evaluate them from the source file itself, after starting up CIDER, by simply running `C-c C-e` after each example -- if it has printing side effects, it'll be thrown in the REPL session, if it's a value, it should appear in a little buffer overlay!


### Running the slides

To run the presentation from emacs, go to `presentation.org` and run `M-x org-present`. You can navigate with left and right, and quit with `C-c C-q`:
* To embiggen the text, do `C-c C-=`
* To run inline code, make sure `CIDER` is running (or run `M-x cider-jack-in`) and do `C-c C-e` to evaluate inline
* To evaluate code blocks, make sure the block has `:results output` as an option, and run `C-c C-c` -- the output will be added in a `RESULTS` section
below the code.

For more code interaction tips, check out the [Babel](https://orgmode.org/worg/org-contrib/babel/intro.html#results) documentation.

