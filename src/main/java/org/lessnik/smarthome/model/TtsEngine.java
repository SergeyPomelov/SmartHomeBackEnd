package org.lessnik.smarthome.model;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyClientBuilder;
import com.amazonaws.services.polly.model.DescribeVoicesRequest;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.Voice;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@ParametersAreNonnullByDefault
public final class TtsEngine {

    private final AmazonPolly polly;
    private final Voice voice;

    public TtsEngine() {

        polly = AmazonPollyClientBuilder.standard()
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .withClientConfiguration(new ClientConfiguration())
                .withRegion(Regions.EU_CENTRAL_1)
                .build();

        var describeVoicesRequest = new DescribeVoicesRequest();
        var describeVoicesResult = polly.describeVoices(describeVoicesRequest);
        voice = describeVoicesResult.getVoices().get(4);
    }

    public InputStream synthesize(String text, OutputFormat format) throws IOException {
        var synthReq =
                new SynthesizeSpeechRequest().withText(text).withVoiceId(voice.getId()).withOutputFormat(format);
        var synthRes = polly.synthesizeSpeech(synthReq);
        return synthRes.getAudioStream();
    }
}
