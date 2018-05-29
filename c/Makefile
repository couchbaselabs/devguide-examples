CPPFLAGS=-Wall -g
LDLIBS=-lcouchbase -pthread

PROGS = \
	bulk-get \
	bulk-store \
	cas \
	connecting \
	connecting-cert-auth \
	connecting-ssl \
	counter \
	create-remove-bucket \
	durability \
	expiration \
	flush \
	fts-basic \
	n1ql-create-primary-index \
	query-atplus \
	query-consistency \
	query-create-index \
	query-criteria \
	query-placeholders \
	retrieving \
	subdoc-retrieving \
	subdoc-updating \
	updating \


all: $(PROGS)

clean:
	rm -f $(PROGS)
	rm -rf *.dSYM
