package io.github.pacman;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Labirinto {
    private Texture bloco;
    private Texture comida;
    private List<Rectangle> paredes;
    private Array<Rectangle> comidas = new Array<>();

    public Labirinto() {
        bloco = new Texture("parede.png");
        comida = new Texture("comida.png");
        paredes = new ArrayList<>();
        comidas = new Array<>();
        carregarLabirinto("modeloLabirinto.txt");
    }
    private void carregarLabirinto(String filePath){
        try(BufferedReader reader =
                new BufferedReader(
                    new FileReader(
                        Gdx.files.internal(filePath).file())
                )
        ){
            String linha;
            int linhaAtual = 0;
            int numeroDeLinhas = 20;
            int alturaBloco =32;
            float alturaTela = Gdx.graphics.getHeight();
            float offSetY = (alturaTela - (numeroDeLinhas * alturaBloco))/2;
            while ((linha = reader.readLine()) != null){
                for(int colunaAtual = 0; colunaAtual < linha.length(); colunaAtual++){
                    char caracter = linha.charAt(colunaAtual);
                    float x = colunaAtual * 32;
                    float y = (offSetY + (numeroDeLinhas - linhaAtual - -1) * alturaBloco) + 40;
                    if(caracter == '#')
                        paredes.add(new Rectangle(x, y, 32, 32));
                    else if(caracter ==  ' ')
                        comidas.add(new Rectangle(x+12, y+12, 8, 8));
                }
                linhaAtual++;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void render(SpriteBatch batch){
        for(Rectangle parede : paredes){
            batch.draw(bloco, parede.x, parede.y, parede.width, parede.height);
        }
        for(Rectangle food : comidas){
            batch.draw(comida, food.x, food.y, food.getWidth(), food.getHeight());
        }
    }

    public boolean colidiuCom(Rectangle rect){
        for(Rectangle parede : paredes){
            if(parede.overlaps(rect)){
                return true;
            }
        }
        return false;
    }

    public void dispose(){
        bloco.dispose();
        comida.dispose();
    }

    public Array<Rectangle> getComidas(){
        return comidas;
    }

}
