package com.github.juggernaut.macchar.session;

/**
 * @author ameya
 */
public class SessionIdAlreadyExists extends IllegalArgumentException {
    public SessionIdAlreadyExists(String s) {
        super(s);
    }
}
