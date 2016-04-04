package com.crone.yandexmobdev.utils;


public class ConvertNumbers {

    // функция переписана с https://habrahabr.ru/post/105428/
    public String convertSongs(int number){
        String sEnding;
        int i;
        number = number % 100;
        if (number>=11 && number<=19) {
            sEnding=String.valueOf(number)+" "+ "песен";
        }
        else {
            i = number % 10;
            switch (i)
            {
                case (1): sEnding = String.valueOf(number)+" "+ "песня"; break;
                case (2):
                case (3):
                case (4): sEnding = String.valueOf(number)+" "+ "песни"; break;
                default: sEnding = String.valueOf(number)+" "+ "песен";
            }
        }
        return sEnding;
    }


    public String convertAlbums(int number){
        String sEnding;
        int i;
        number = number % 100;
        if (number>=11 && number<=19) {
            sEnding=String.valueOf(number)+" "+ "альбомов";
        }
        else {
            i = number % 10;
            switch (i)
            {
                case (1): sEnding = String.valueOf(number)+" "+ "альбом"; break;
                case (2):
                case (3):
                case (4): sEnding = String.valueOf(number)+" "+ "альбома"; break;
                default: sEnding = String.valueOf(number)+" "+ "альбомов";
            }
        }
        return sEnding;
    }



}
