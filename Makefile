joosc:
	activator assembly
	echo '#!/bin/bash\njava -jar target/scala-2.11/juicy.jar "$$@"' > joosc
	chmod +x joosc

.PHONY: clean
clean:
	rm joosc
