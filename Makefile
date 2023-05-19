dc=docker compose
logs=docker logs
exec=docker exec -it
publish=community-aem-publish-1
author=community-aem-author-1
scripts_path=/opt/aem/docker/scripts

# AEM build & test commands
publish-build:
	${exec} ${publish} ${scripts_path}/build-utilities.sh build-no-tests

publish-restore:
	${exec} ${publish} ${scripts_path}/config-utilities.sh setup-quickstart && ${dc} restart publish

author-build:
	${exec} ${author} ${scripts_path}/build-utilities.sh build-no-tests

author-restore:
	${exec} ${author} ${scripts_path}/config-utilities.sh setup-quickstart && ${dc} restart author

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
