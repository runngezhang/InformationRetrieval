main.pdf: main.tex bibliography.bib 
	pdflatex main
	bibtex main
	pdflatex main
	pdflatex main

clean:
	rm -f main.aux main.log main.bbl main.blg main.dvi main.toc 

