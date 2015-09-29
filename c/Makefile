PROGS=connecting updating retrieving query-create-index query-criteria \
	  query-placeholders counter expiration

all: $(PROGS)

clean:
	rm -f $(PROGS)
	rm -rf *.dSYM

CPPFLAGS=-Wall -g
LDFLAGS=-lcouchbase
