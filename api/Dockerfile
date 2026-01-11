FROM  amazoncorretto:21.0.9-al2023-headless

# Install fontconfig and NanumGothic font for card news image generation (if not exists)
RUN if ! fc-list 2>/dev/null | grep -qi "NanumGothic"; then \
        dnf install -y fontconfig wget unzip && \
        mkdir -p /usr/share/fonts/nanumgothic && \
        cd /tmp && \
        wget https://github.com/naver/nanumfont/releases/download/VER2.6/NanumGothic.zip && \
        unzip NanumGothic.zip -d /usr/share/fonts/nanumgothic && \
        rm NanumGothic.zip && \
        fc-cache -fv && \
        dnf clean all && \
        echo "NanumGothic font installed successfully"; \
    else \
        echo "NanumGothic font already exists, skipping installation"; \
    fi

RUN mkdir -p /logs

ENV	PROFILE default
ENV TZ=Asia/Seoul
EXPOSE 8080

ARG JAVA_OPTS

ARG RELEASE_VERSION
ENV DD_VERSION=${RELEASE_VERSION}

ARG JAR_FILE="build/libs/api.jar"
COPY ${JAR_FILE} app.jar

ENTRYPOINT java -XX:MaxGCPauseMillis=100 -XX:InitialRAMPercentage=50.0 -XX:MaxRAMPercentage=80.0 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 $JAVA_OPTS -jar app.jar
