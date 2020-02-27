package com.github.juggernaut.muqtti.session;

/**
 * @author ameya
 */
public class SessionIdAlreadyExists extends IllegalArgumentException {
    public SessionIdAlreadyExists(String s) {
        super(s);
    }
}
