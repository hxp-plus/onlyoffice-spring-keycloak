FROM ubuntu:20.04

ENV TZ=Etc/UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ >/etc/timezone

RUN apt-get -y update && apt-get -y install python2 python3 git sudo
RUN ln -s /usr/bin/python2 /usr/bin/python
WORKDIR /build

RUN git clone https://github.com/ONLYOFFICE/build_tools.git
RUN cd /build/build_tools/tools/linux && ./automate.py server
RUN sed -i 's/exports.LICENSE_CONNECTIONS = 20;/exports.LICENSE_CONNECTIONS = 99999;/' /build/server/Common/sources/constants.js
RUN sed -i 's/"--update", "1"/"--update", "0"/' /build/build_tools/tools/linux/automate.py
RUN cd /build/build_tools/tools/linux && ./automate.py server

FROM ubuntu:20.04
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ >/etc/timezone
COPY --from=0 /build/build_tools/out/linux_64/onlyoffice/documentserver /var/www/onlyoffice/documentserver
RUN apt-get -y update && apt-get -y install sudo vim ttf-wqy-zenhei fonts-wqy-microhei supervisor
COPY production-linux.json /var/www/onlyoffice/documentserver/server/Common/config/production-linux.json
RUN cd /var/www/onlyoffice/documentserver && \
  mkdir fonts && \
  LD_LIBRARY_PATH=${PWD}/server/FileConverter/bin server/tools/allfontsgen \
  --input="${PWD}/core-fonts" \
  --allfonts-web="${PWD}/sdkjs/common/AllFonts.js" \
  --allfonts="${PWD}/server/FileConverter/bin/AllFonts.js" \
  --images="${PWD}/sdkjs/common/Images" \
  --selection="${PWD}/server/FileConverter/bin/font_selection.bin" \
  --output-web='fonts' \
  --use-system="true" &&  \
  LD_LIBRARY_PATH=${PWD}/server/FileConverter/bin server/tools/allthemesgen \
  --converter-dir="${PWD}/server/FileConverter/bin" \
  --src="${PWD}/sdkjs/slide/themes" \
  --output="${PWD}/sdkjs/common/Images"
RUN mkdir -p /var/log/documentserver/docservice && mkdir -p /var/log/documentserver/converter
COPY supervisord.conf /etc/supervisor/supervisord.conf
CMD [ "/usr/bin/supervisord" ]
