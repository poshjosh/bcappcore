/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bc.appcore.functions;

import com.bc.jpa.EntityMemberAccess;
import com.bc.jpa.context.PersistenceUnitContext;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on May 9, 2018 3:00:19 PM
 */
public class GetNamedGetter implements Serializable, BiFunction<Class, String, Method> {

    private final PersistenceUnitContext puContext;
    
    private final Map<Class, EntityMemberAccess> typeToMemberAccessMap;

    public GetNamedGetter(PersistenceUnitContext puContext) {
        this.puContext = Objects.requireNonNull(puContext);
        this.typeToMemberAccessMap = new HashMap();
    }
    
    @Override
    public Method apply(Class type, String name) {
        final EntityMemberAccess memberAccess = this.getMemberAccess(type);
        return memberAccess.getMethod(false, name);
    }
    
    public EntityMemberAccess getMemberAccess(Class type) {
        EntityMemberAccess memberAccess = this.typeToMemberAccessMap.get(type);
        if(memberAccess == null) {
            memberAccess = this.puContext.getEntityMemberAccess(type);
            this.typeToMemberAccessMap.put(type, Objects.requireNonNull(memberAccess));
        }
        return memberAccess;
    }
}
