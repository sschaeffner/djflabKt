FROM debian:12 as build-snapcast

ARG GIT_COMMIT="v0.27.0"
ARG BOOST_VERSION="1_83_0"

RUN apt-get update && apt-get upgrade -y --no-install-recommends
RUN apt-get install -y --no-install-recommends ca-certificates wget git build-essential cmake libasound2-dev libpulse-dev libvorbisidec-dev libvorbis-dev libopus-dev libflac-dev libsoxr-dev alsa-utils libavahi-client-dev avahi-daemon libexpat1-dev

WORKDIR /boost
RUN wget https://boostorg.jfrog.io/artifactory/main/release/1.83.0/source/boost_${BOOST_VERSION}.tar.gz -O boost.tar.gz \
    && tar xf ./boost.tar.gz \
    && rm ./boost.tar.gz \
    && mv ./boost* ./boost

WORKDIR /app

RUN git clone https://github.com/badaix/snapcast.git \
    && cd snapcast \
    && git checkout ${GIT_COMMIT}

RUN cd snapcast \
    && mkdir build \
    && cd build \
    && cmake .. -DBUILD_SERVER=ON -DBUILD_CLIENT=ON -DBUILD_WITH_PULSE=OFF -DBOOST_ROOT=/boost/boost \
    && cmake --build .

FROM node as build-snapweb
ARG GIT_COMMIT="react"

RUN apt-get update && apt-get upgrade -y --no-install-recommends

WORKDIR /app

RUN git clone https://github.com/badaix/snapweb.git \
    && cd snapweb \
    && git checkout ${GIT_COMMIT}

RUN cd snapweb \
    && npm ci \
    && npm run build


FROM debian:12
WORKDIR /app

RUN apt-get update && apt-get upgrade -y --no-install-recommends
RUN apt-get install -y --no-install-recommends  \
    procps \
    libasound2-dev  \
    libpulse-dev  \
    libvorbisidec-dev  \
    libvorbis-dev  \
    libopus-dev  \
    libflac-dev  \
    libsoxr-dev  \
    alsa-utils  \
    libavahi-client-dev  \
    avahi-daemon  \
    libnss-mdns \
    libexpat1-dev \
    libtool  \
    libpopt-dev  \
    libconfig-dev  \
    libssl-dev

COPY --from=build-snapcast /app/snapcast/bin/* /app/
COPY --from=build-snapweb /app/snapweb/build /app/snapweb
# COPY avahi.conf /etc/avahi/avahi-daemon.conf
CMD mkdir -p /run/dbus
COPY run.sh /app/run.sh

#ENTRYPOINT /app/snapserver
CMD /app/run.sh
EXPOSE 1704 1705 1780
