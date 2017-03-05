/*
  ISoundGenerator.java

  (c) 2016 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.sound;

/**
 * Created by ejs on 3/8/16.
 */
public interface ISoundGenerator {
    /**
     * Generate the given number of samples immediately
     * @param samples
     * @return
     */
    void generate(int samples);
}
