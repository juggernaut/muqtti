package com.github.juggernaut.macchar.packet;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author ameya
 */
public class UtilsTest {

    @Test
    public void testMultiLevelWildcardMatching() {
        final String filter = "#";
        final String topic = "places/india";
        assertTrue(Utils.doesSubscriptionMatchTopic(filter, topic));
    }

    @Test
    public void testSingleLevelWildcardMatching() {
        final String filter = "devices/+";
        final String topic = "devices/1";
        assertTrue(Utils.doesSubscriptionMatchTopic(filter, topic));
    }

    @Test
    public void testFilterLongerThanTopicDoesNotMatch() {
        final String filter = "devices/lights/#";
        final String topic = "devices/lights";
        assertFalse(Utils.doesSubscriptionMatchTopic(filter, topic));
    }

    @Test
    public void testExactMatch() {
        final String filter = "places/india/hubli";
        final String topic = "places/india/hubli";
        assertTrue(Utils.doesSubscriptionMatchTopic(filter, topic));
    }
}
