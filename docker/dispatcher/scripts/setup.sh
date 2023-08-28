#!/bin/sh
#
# Copyright (c) 2023 Adobe Systems Incorporated. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
DISPARCH=x86_64

if [ "${TARGETARCH}" = "arm64" ]; then
    DISPARCH=aarch64
fi

##create default docroots
#mkdir -p /mnt/var/www/html
#chown apache:apache /mnt/var/www/html
#
#mkdir -p /mnt/var/www/default
#chown apache:apache /mnt/var/www/default
#
#mkdir -p /mnt/var/www/author
#chown apache:apache /mnt/var/www/author

##set up dispatcher
#mkdir -p /tmp/dispatcher
#
#curl -o /tmp/dispatcher/dispatcher.tar.gz https://download.macromedia.com/dispatcher/download/dispatcher-apache2.4-linux-$DISPARCH-4.3.5.tar.gz
#
#cd /tmp/dispatcher
#
#tar zxvf dispatcher.tar.gz
#
#cp -v dispatcher-apache2.4-4.3.5.so /etc/httpd/modules/mod_dispatcher.so

# TODO: is ssl required? once done or not remove setup.sh
#set up haproxy SSL

#mkdir -p /etc/ssl/docker && \
    openssl req -new -newkey rsa:4096 -days 365 -nodes -x509 -subj "/C=GB/ST=London/L=London/O=Adobe/CN=localhost" \
     -keyout /etc/ssl/docker/localhost.key \
     -out /etc/ssl/docker/localhost.crt && \
    cat /etc/ssl/docker/localhost.key /etc/ssl/docker/localhost.crt > /etc/ssl/docker/haproxy.pem
