 boolean isMessage=false;

#include <Servo.h>     // Adiciona a biblitoeca Servo

//Definindo os pinos
#define trigPin A0     //Pino TRIG do sensor no pino analógico A0
#define echoPin A1     //Pino ECHO do sensor no pino analógico A1
//motor um             // Ligação dos pinos da Ponte H L298N
#define enA  11        //pino enA na porta digital 10
#define in1  9         //pino in1 na porta digital 9
#define in2  8         //pino in2 na porta digital 8
// motor dois          // Ligação dos pinos da Ponte H L298N
#define enB  5         //pino enB na porta digital 5
#define in3  7         //pino in3 na porta digital 7
#define in4  6         //pino in4 na porta digital 6

Servo servoSensor;       // Crie um objeto Servo para controlar o Servo.

int velocidadeMotorUm = 180;  //velocidade do motor da esquerda
int velocidadeMotorDois = 112; //velocidade do motor da direita
float Distancia = 0.00;        // variavel para guardar a distancia

String leStringSerial(){
  String conteudo = "";
  char caractere;
  
  // Enquanto receber algo pela serial
  while(Serial.available() > 0) {
    // Lê byte da serial
    caractere = Serial.read();
    // Ignora caractere de quebra de linha
    if (caractere != '\n'){
      // Concatena valores
      conteudo.concat(caractere);
    }
    // Aguarda buffer serial ler próximo caractere
    delay(10);
  }
    
  Serial.print("Recebi: ");
  Serial.println(conteudo);
    
  return conteudo;
}

void setup()
{
  Serial.begin(9600);
  //Define o servo na porta 13
  servoSensor.attach(13); 
  pinMode(enA, OUTPUT);
  pinMode(enB, OUTPUT);
  pinMode(in1, OUTPUT);
  pinMode(in2, OUTPUT);
  pinMode(in3, OUTPUT);
  pinMode(in4, OUTPUT);

  //Configuraçõs do sensor ultrassonico
  pinMode(trigPin, OUTPUT);     //define o pino TRIG como saída
  pinMode(echoPin, INPUT);      //define o pino ECHO como entrada
}

//função para procurar obtasculo a todo o tempo
int Procurar (void) {
  float duracao = 0.0;              // variavael para quartar a duração do retorno do som
  float CM = 0.0;                   // variavael para quartar a distancia
  digitalWrite(trigPin, LOW);       //não envia som
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);      //envia som
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);       //não envia o som e espera o retorno do som enviado
  duracao = pulseIn(echoPin, HIGH); //Captura a duração em tempo do retorno do som.
  CM = (duracao / 58.8);            //Calcula a distância em centimetros
  Serial.print("Distancia em CM: ");
  Serial.println(CM);               //Imprimi no monitor serial a distancia
  return CM;                        
}

void Frente()
{
  Serial.println("Robô: Frente ");
  digitalWrite(in1, HIGH);                          //Configurar a ponte h 
  digitalWrite(in2, LOW);
  digitalWrite(in3, HIGH);
  digitalWrite(in4, LOW);
  analogWrite(enA, velocidadeMotorUm);              // Defina a velocidade do motor Um
  analogWrite(enB, velocidadeMotorDois);            // Defina a velocidade do motor Dois                         
}

void ParaTras()
{
  Serial.println("Robô: Ré ");
  digitalWrite(in1, LOW);                           //Configurar a ponte h 
  digitalWrite(in2, HIGH);
  digitalWrite(in3, LOW);
  digitalWrite(in4, HIGH);
  delay(300);                                       //aguarda um tempo
  analogWrite(enA, velocidadeMotorUm);              // Defina a velocidade do motor Um
  analogWrite(enB, velocidadeMotorDois);            // Defina a velocidade do motor Dois                         
}

void Parar()
{
  Serial.println("Robô: Parar ");
  digitalWrite(in1, LOW);                           //Configurar a ponte h 
  digitalWrite(in2, LOW);
  digitalWrite(in3, LOW);
  digitalWrite(in4, LOW);
  delay(100);                                       //aguarda um tempo
}

void Emergencia()
{
  Serial.println("Robô: Frente ");
  digitalWrite(in1, HIGH);                          //Configurar a ponte h 
  digitalWrite(in2, LOW);
  digitalWrite(in3, HIGH);
  digitalWrite(in4, LOW);
  analogWrite(enA, velocidadeMotorUm);              // Defina a velocidade do motor Um
  analogWrite(enB, velocidadeMotorDois);            // Defina a velocidade do motor Dois                         
}

void loop()
{
  char c;
  servoSensor.write (80);       // Gira o Servo com o sensor a 90 graus
  delay (100);                  // Aguarda 100 milesugodos
  Distancia = Procurar ();      // Medindo a Distancia em CM.
  if(Distancia < 40) {
    Parar();
  }
  if(Serial.available()){
    String recebido = leStringSerial();
    
    isMessage= !isMessage;
    if(recebido == "frente"){
      Frente();    
    }
    if(recebido =="tras"){
      ParaTras();
    }
    if(recebido =="parar"){
      Parar();  
    }
    if(recebido =="emergencia"){
      Emergencia();
    }
  }
}
