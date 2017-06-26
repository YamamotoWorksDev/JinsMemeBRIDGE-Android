/**
 * DialogListener.java
 *
 * Copylight (C) 2017, Shunichi Yamamoto(Yamamoto Works Ltd.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 **/

package com.jins_meme.bridge;

import java.util.EventListener;

public interface DialogListener extends EventListener {

  void doPositiveClick(String type);
  void doNegativeClick(String type);
}
