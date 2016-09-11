package org.infotex.skondurushkin.factorizer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(value = Include.ALWAYS)
public class Factor {
    long	factor;
    int		power;
    public Factor(long factor, int power) {
        this.factor = factor;
        this.power = power;
    }

    @JsonProperty(value = "f")
    public long getFactor() {
        return this.factor;
    }
    @JsonProperty(value = "p")
    public int getPower() {
        return this.power;
    }
    @Override
    public String toString() {
        return String.format("%d^%d", factor, power);
    }
    @Override
    public boolean equals(Object other) {
        if (other instanceof Factor) {
            Factor o = (Factor)other;
            return o.factor == this.factor && o.power == this.power;
        }
        return false;
    }
    public long restore() {
        long ret = factor;
        for (int i = 1; i < power; ++i) {
                ret *= factor;
        }
        return ret;
    }
}