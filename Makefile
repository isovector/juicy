DEPENDS := $(shell find src -iname '*.scala')

joosc: ${DEPENDS}
	chmod +x ./activator
	./activator assembly
	echo '#!/bin/bash\njava -jar target/scala-2.11/juicy.jar "$$@"' > joosc
	chmod +x ./joosc

.PHONY: clean marmoset
clean:
	rm joosc

marmoset:
	zip -r juicy.zip * -x "*target*" -x "*.swp" -x "joosc-test*" -x "stdlib*" -x "docs*" -x "test*" -x joosc
