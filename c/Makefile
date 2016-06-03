PROGS=connecting updating retrieving query-create-index query-criteria \
	  query-placeholders counter expiration \
	  query-consistency cas durability bulk-get bulk-store \
	  connecting-ssl subdoc-retrieving subdoc-updating fts-basic \
	  create-remove-bucket

all: $(PROGS)

clean:
	rm -f $(PROGS)
	rm -rf *.dSYM

CPPFLAGS=-Wall -g
LDLIBS=-lcouchbase
