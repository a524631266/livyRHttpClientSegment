package com.hw.transmitlayer.service.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.Future;

interface RHttpHandlerInterface {
    Future submitcode(String code) throws IOException, URISyntaxException;
}
