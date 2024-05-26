FROM ubuntu:20.04

ENV TZ=Etc/UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN apt-get -y update && \
    apt-get -y install python2 \
                       python3 \
                       sudo \
                       git \
                       postgresql \
                       rabbitmq-server \
                       nginx
RUN ln -s /usr/bin/python2 /usr/bin/python
WORKDIR /build

RUN git clone https://github.com/ONLYOFFICE/build_tools.git
RUN cd /build/build_tools/tools/linux && ./automate.py server
RUN sed -i 's/exports.LICENSE_CONNECTIONS = 20;/exports.LICENSE_CONNECTIONS = 99999;/' /build/server/Common/sources/constants.js
RUN sed -i 's/"--update", "1"/"--update", "0"/' /build/build_tools/tools/linux/automate.py
RUN cd /build/build_tools/tools/linux && ./automate.py server

FROM ubuntu:20.04
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
COPY --from=0 /build/build_tools/out/linux_64/onlyoffice/documentserver /var/www/html/documentserver
RUN apt-get -y update && apt-get -y install nginx postgresql rabbitmq-server sudo ttf-wqy-zenhei fonts-wqy-microhei
RUN sudo rm -f /etc/nginx/sites-enabled/default
COPY onlyoffice-documentserver /etc/nginx/sites-available/onlyoffice-documentserver
RUN ln -s /etc/nginx/sites-available/onlyoffice-documentserver /etc/nginx/sites-enabled/onlyoffice-documentserver
RUN service postgresql restart && \
    sudo -i -u postgres psql -c "CREATE DATABASE onlyoffice;" && \
    sudo -i -u postgres psql -c "CREATE USER onlyoffice WITH password 'onlyoffice';" && \
    sudo -i -u postgres psql -c "GRANT ALL privileges ON DATABASE onlyoffice TO onlyoffice;" && \
    PGPASSWORD=onlyoffice psql -hlocalhost -Uonlyoffice -d onlyoffice -f /var/www/html/documentserver/server/schema/postgresql/createdb.sql
RUN cd /var/www/html/documentserver && \
    mkdir fonts && \
    LD_LIBRARY_PATH=${PWD}/server/FileConverter/bin server/tools/allfontsgen \
      --input="${PWD}/core-fonts" \
      --allfonts-web="${PWD}/sdkjs/common/AllFonts.js" \
      --allfonts="${PWD}/server/FileConverter/bin/AllFonts.js" \
      --images="${PWD}/sdkjs/common/Images" \
      --selection="${PWD}/server/FileConverter/bin/font_selection.bin" \
      --output-web='fonts' \
      --use-system="true" && \
    LD_LIBRARY_PATH=${PWD}/server/FileConverter/bin server/tools/allthemesgen \
      --converter-dir="${PWD}/server/FileConverter/bin"\
      --src="${PWD}/sdkjs/slide/themes"\
      --output="${PWD}/sdkjs/common/Images"
COPY start.sh /start.sh
CMD /bin/bash /start.sh

