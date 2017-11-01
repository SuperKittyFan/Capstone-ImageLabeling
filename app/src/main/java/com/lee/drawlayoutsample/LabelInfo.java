package com.lee.drawlayoutsample;

import java.util.List;

/**
 * Created by zhuchen on 2017/11/1.
 */

public class LabelInfo {

        private int numofSquare;
        private List<Square> square;
        public void setNumofSquare(int numofSquare) {
            this.numofSquare = numofSquare;
        }
        public int getNumofSquare() {
            return numofSquare;
        }

        public void setSquare(List<Square> square) {
            this.square = square;
        }
        public List<Square> getSquare() {
            return square;
        }

}
