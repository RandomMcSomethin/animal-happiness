package io.github.randommcsomethin.animalhappiness.config;

import io.github.randommcsomethin.animalhappiness.AnimalHappiness;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnimalEntry {
    String ID;
    List<Parameter> parameters;

    public AnimalEntry() {}

    public AnimalEntry(String ID, String[] parameters) {
        this.ID = ID;
        this.parameters = new ArrayList<>();
        if (parameters != null) {
            for (String p : parameters) {
                this.parameters.add(new Parameter(p));
            }
        }
    }

    public static AnimalEntry withName(String id) {
        return new AnimalEntry(id, null);
    }

    // returns this entry's ID
    public String getID() {
        return this.ID;
    }

    // returns the arguments of a parameter (if applicable)
    public String[] getParameter(String id) {
        for (Parameter p : this.parameters) {
            if (p.getID().equals(id)) {
                AnimalHappiness.LOGGER.info("Parameter found: " + id + " for entry " + this.getID());
                return p.getArgs();
            }
        }
        AnimalHappiness.LOGGER.info("Parameter not found: " + id + " for entry " + this.getID());
        return null;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof AnimalEntry && this.ID.equals(((AnimalEntry) o).ID);
    }
}

class Parameter {
    String data;

    public Parameter() {}

    public Parameter(String args) {
        this.data = args;
    }

    public String[] getArgs() {
        return data.split(":");
    }

    public String getID() {
        String[] args = this.getArgs();
        return args[0] + ":" + args[1];
    }
}
