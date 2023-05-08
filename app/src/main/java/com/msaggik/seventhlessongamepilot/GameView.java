package com.msaggik.seventhlessongamepilot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements Runnable{

    // поля
    private  Thread thread; // поле нового потока
    private boolean isPlaying; // поле запуска и приостановления игры
    private Background background1, background2; // поля работы с фоном (необходимо два, что было непрерывное движение фона)
    private int screenX, screenY; // поля размеров экрана по осям X и Y
    private Paint paint; // поле стилей рисования
    private float screenRatioX, screenRatioY; // поля размеров экрана для совместимости разных размеров экрана
    private Flight flight; // создание поля самолёта

    // конструктор на основе SurfaceView
    public GameView(Context context, int screenX, int screenY) {
        super(context);
        this.screenX = screenX;
        this.screenY = screenY;

        screenRatioX = 1920f / screenX; // калибровка совместимости оси X
        screenRatioY = 1080f / screenY; // калибровка совместимости оси Y

        // создание объектов фонов (размеры, ранее созданный ресурс)
        background1 = new Background(screenX, screenY, getResources());
        background2 = new Background(screenX, screenY, getResources());

        // присваивание полю x класса Background переменной ширины screenX
        background2.setX(screenX); // второй фон мы сдвигаем по оси Х с нуля на размер ширины изображения

        paint = new Paint(); // создание объекта стиля рисования

        // создание объекта самолёта
        flight = new Flight(screenX, screenY, getResources());
    }

    // реализация метода run() дополнительного потока
    @Override
    public void run() {
        // операции в потока
        while (isPlaying) {
            // методы запускаемые в потоке
            update();
            draw();
            sleep();
        }
    }
    // метод обновления потока
    private void update() {
        // сдвиг фона по оси X на 10 пикселей и преобразование для совместимости разных экранов
        background1.setX(background1.getX() - (int)(10 * screenRatioX));
        background2.setX(background2.getX() - (int)(10 * screenRatioX));

        if ((background1.getX() + background1.getBackground().getWidth()) <= 0) { // если фон 1 полностью исчез с экрана
            background1.setX(screenX); // то обновление x до размера ширины фона
        }
        if ((background2.getX() + background2.getBackground().getWidth()) <= 0) { // если фон 2 полностью исчез с экрана
            background2.setX(screenX); // то обновление x до размера ширины фона
        }

        // задание скорости подъёма и снижения самолёта
        if (flight.isGoingUp()) { // условие подъёма
            flight.setY(flight.getY() - (int)(30 * screenRatioY));
        } else { // условие снижения
            flight.setY(flight.getY() + (int)(30 * screenRatioY));
        }
        // задание порога значений местоположения самолёта
        if (flight.getY() < 0) { // запрет на снижение меньше нуля
            flight.setY(0);
        } else if (flight.getY() >= screenY - flight.getHeight()) { // запрет на подъём выше экрана за минусом высоты самолёта
            flight.setY(screenY - flight.getHeight());
        }
    }

    // метод рисования в потоке
    private void draw() {

        if (getHolder().getSurface().isValid()) { // проверка валидности объекта surface

            Canvas canvas = getHolder().lockCanvas(); // метод lockCanvas() возвращает объект Canvas (холст для рисования)
            // метод drawBitmap() рисует растровое изображение фона на холсте (изображение, координаты X и Y, стиль для рисования)
            canvas.drawBitmap(background1.getBackground(), background1.getX(), background1.getY(), paint);
            canvas.drawBitmap(background2.getBackground(), background2.getX(), background2.getY(), paint);

            // отрисовка растрового изображения самолёта
            canvas.drawBitmap(flight.getFlight(), flight.getX(), flight.getY(), paint);

            // вывод нарисованных изображений на экран
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    // метод засыпания потока
    private void sleep() {
        try {
            // засыпание потока на 16 милисекунд
            Thread.sleep(16);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // метод запуска потока
    public void resumeThread() {
        // установление флага запуска игры
        isPlaying = true;
        // создание объекта потока
        thread = new Thread(this);
        // запуск потока
        thread.start();
    }
    // метод паузы потока
    public void pauseThread() {
        try {
            // установление флага приостановления игры
            isPlaying = false;
            // приостановление потока
            thread.join();
        } catch (InterruptedException e) { // исключение на случай зависания потока
            e.printStackTrace();
        }
    }

    // метод обработки касания экрана (для управления самолётом)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // handle screen touch events
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN: // click
                // Set initial position of plane to middle of the screen along the Y-axis
                float planeWidth = flight.getWidth(); // Assuming getWidth() returns the width of the plane
                float planeHeight = flight.getHeight(); // Assuming getHeight() returns the height of the plane
                int middleX = (int) (screenX / 2 - planeWidth / 2); // Calculate middle X-coordinate
                int middleY = (int) (screenY / 2 - planeHeight); // Calculate Y-coordinate of the bottom of the plane
                flight.setX(middleX); // Set X-coordinate of plane
                flight.setY(middleY); // Set Y-coordinate of plane
                flight.setGoingUp(true); // Set the plane to go up

                break;
            case MotionEvent.ACTION_MOVE: // move around the screen
                // Check if the user pressed on the left side of the screen
                if (event.getY() < (screenX / 2)) {
                    flight.setGoingUp(true); // Set the plane to go up
                } else if (event.getY() >= (screenX / 2)){
                    flight.setGoingUp(false); // Set the plane to go down
                }
                break;
            case MotionEvent.ACTION_UP: // release
                flight.setGoingUp(false); // Set the plane to go down
                break;
        }

        return true; // activate screen touch handling
    }

}