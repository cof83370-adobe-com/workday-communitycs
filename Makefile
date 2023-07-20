dc=docker compose
logs=docker logs
exec=docker exec -it
publish=community-aem-publish-1
author=community-aem-author-1
scripts_path=/opt/aem/docker/scripts

# AEM build & test commands
aem-build:
	${exec} ${publish} ${scripts_path}/build-utilities.sh build-no-tests

unit-tests:
	${exec} ${author} ${scripts_path}/build-utilities.sh unit-tests

integration-tests:
	${exec} ${author} ${scripts_path}/build-utilities.sh integration-tests

pipeline-check:
	${exec} ${author} ${scripts_path}/build-utilities.sh pipeline-check

# AEM Docker Aliases
aem:
	${dc} up -d

aem-down:
	${dc} down

aem-stop:
	${dc} stop

aem-start:
	${dc} start

aem-restart:
	${dc} restart

aem-pause:
	${dc} pause

aem-unpause:
	${dc} unpause

publish-log:
	${logs} -f community-aem-publish-1

author-log:
	${logs} -f community-aem-publish-1

dispatcher-start:
	${dc} -f docker-compose.dispatcher.yaml -f docker-compose.yaml up -d

dispatcher-stop:
	${dc} -f docker-compose.dispatcher.yaml -f docker-compose.yaml stop

dispatcher-down:
	${dc} -f docker-compose.dispatcher.yaml -f docker-compose.yaml down

restart-apache:
	${exec} community-aem-dispatcher-1 sh -c "kill -HUP `cat /var/run/httpd/httpd.pid`"

