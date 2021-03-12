Un script babashka simple pour générer des lettres en français à partir de fichiers edns.

Nécessite [babashka](https://github.com/babashka/babashka) et [pdflatex](https://www.tug.org/applications/pdftex/) installés. pdflatex vient avec la plupart des distributions LaTeX existantes.

    (def cli-options
      ;; An option with a required argument
      [["-t" "--template TEMPL" "LaTeX template"
        :default "./letter-template.tex"]
       ["-i" "--input INPUT" "edn letter"
        :default-fn (constantly *in*)]
       ["-m" "--message MESSAGE" "edn letter message"]
       ["-f" "--from EDN" "expeditor edn file"]
       ["-2" "--to EDN" "receiver edn file"]
       ["-o" "--output OUT" "output name, will generate out.edn and out.pdf"
        :default "./letter"]
       ["-h" "--help"]])
