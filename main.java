class Quad{
  PVector lowerLeft;
  float size;
  public Quad(PVector ll, float s){
    lowerLeft = ll;
    size = s;
  }
  
  public boolean contains(float x, float y){
    float top = lowerLeft.y - size;
    float left = lowerLeft.x;
    float bottom = lowerLeft.y;
    float right = lowerLeft.x + size;
    if(x > left && x < right && y > top && y < bottom) return true;
    return false;
  }
  
  float getSize(){
    return size;
  }
  
  PVector[] getBounds(){
    PVector bl = new PVector(lowerLeft.x, lowerLeft.y);
    PVector br = new PVector(lowerLeft.x + size, lowerLeft.y);
    PVector tr = new PVector(lowerLeft.x + size, lowerLeft.y + size);
    PVector tl = new PVector(lowerLeft.x, lowerLeft.y + size);
    PVector[] bounds = {bl, br, tr, tl}; // bottom left -> bottom right -> top right -> top left
    return bounds;  
  }
  
  Quad NW(){
    PVector ll = new PVector(lowerLeft.x, lowerLeft.y - getSize() / 2);
    return new Quad(ll, getSize() / 2);
  }
  Quad NE(){
    PVector ll = new PVector(lowerLeft.x + getSize() / 2, lowerLeft.y - getSize() / 2);
    return new Quad(ll, getSize() / 2);
  }
  Quad SE(){
    PVector ll = new PVector(lowerLeft.x + getSize() / 2, lowerLeft.y);
    return new Quad(ll, getSize() / 2);
  }
  Quad SW(){
    PVector ll = new PVector(lowerLeft.x, lowerLeft.y);
    return new Quad(ll, getSize() / 2);
  }
}

class Body{
  float x;
  float y;
  float px;
  float py;
  float vx;
  float vy;
  float mass;
  int id;
  public Body(float xPos, float yPos, float pxPos, float pyPos, float m, int i){
    x = xPos;
    y = yPos;
    px = pxPos;
    py = pyPos;
    mass = m;
    vx = 0;
    vy = 0;
    id = i;
  }
  
  public boolean in(Quad q){
    float top = q.lowerLeft.y - q.size;
    float left = q.lowerLeft.x;
    float bottom = q.lowerLeft.y;
    float right = q.lowerLeft.x + q.size;
    
    if(x > left && x < right && y > top && y < bottom) return true;
    return false;
  }
  
  Body add(Body a, Body b){
    return new Body(0, 0, 10, 10, 10, bodies.size());
  }
  
  public void updatePosition(){
    x += vx;
    y += vy;
  }
  
}

class BHTree {
  private Body body;
  public Body bodyArchive;
  private Quad quad;
  private BHTree NW;
  private BHTree NE;
  private BHTree SW;
  private BHTree SE;
  public BHTree parent;
  
  private int level;
  
  //center of mass
  private float px;
  private float py;
  private float mass = 0;
  
  public boolean empty;
  
  public BHTree(Quad q, int l, BHTree p){
    quad = q;
    level = l;
    parent = p;
    empty = false;
  }
  public void insert(Body b){
    if((!empty && NW == null)){
      body = b;
      px = body.x;
      py = body.y;
      mass += body.mass;
      empty = true;
      bodyArchive = new Body(b.x, b.y, b.px, b.py, b.mass, b.id);
      
    } else {
      //update center of mass
      px = (px * mass + b.x * b.mass) / (mass + b.mass);
      py = (py * mass + b.y * b.mass) / (mass + b.mass);
      mass += b.mass;
      
      Body other = null;
      if(body != null && empty == false) other = new Body(body.x, body.y, body.px, body.py, body.mass, body.id); //body
      body = null;
      empty = false;
      if (NW == null) NW = new BHTree(quad.NW(), level + 1, this);
      if (NE == null) NE = new BHTree(quad.NE(), level + 1, this);
      if (SW == null) SW = new BHTree(quad.SW(), level + 1, this);
      if (SE == null) SE = new BHTree(quad.SE(), level + 1, this);
      if(other != null){
        if(NW.quad.contains(other.x, other.y)){
          NW.insert(other);
        } else if(NE.quad.contains(other.x, other.y)){
          NE.insert(other);
        } else if(SW.quad.contains(other.x, other.y)){
          SW.insert(other);
        } else if(SE.quad.contains(other.x, other.y)){
          SE.insert(other);
        }
      }
      if(NW.quad.contains(b.x, b.y)){
        NW.insert(b);
      } else if(NE.quad.contains(b.x, b.y)){
        NE.insert(b);
      } else if(SW.quad.contains(b.x, b.y)){
        SW.insert(b);
      } else if(SE.quad.contains(b.x, b.y)){
        SE.insert(b);
      }
    }
  }
  
  void updateForces(Body b){
    if(bodyArchive == null) return;
    float theta = 0.5;
    if(NW == null){// if this is an external node
      if(b != body && b != bodyArchive){
        PVector force = calculateForce(b.x, b.y, b.mass, bodyArchive.x, bodyArchive.y, bodyArchive.mass);
        b.vx += force.x;
        b.vy += force.y;
      }
    }
    float sd = quad.getSize() / dist(b.x, b.y, bodyArchive.x, bodyArchive.y);
    
    if(sd < theta){
        PVector force = calculateForce(b.x, b.y, b.mass, px, py, mass);
        b.vx += force.x;
        b.vy += force.y;
    } else {
      if(NW != null) NW.updateForces(b);
      if(NW != null) NE.updateForces(b);
      if(NW != null) SW.updateForces(b);
      if(NW != null) SE.updateForces(b);
    }
  }
  
  PVector calculateForce(float x1, float y1, float m1, float x2, float y2, float m2){
    float angle = atan2(y2 - y1, x2 - x1);
    float force = (m1 * m2) / dist(x1, y1, x2, y2) * G;
    
    float fx = cos(angle) * force;
    float fy = sin(angle) * force;
    
    return new PVector(fx, fy);
  }
  
  void detectCollisions(){
    if (body != null){
       if(parent != null){
         if(parent.NW.body != null && parent.NW != this){
           if(dist(body.x, body.y, parent.NW.body.x, parent.NW.body.y) < 10){
             //collision
             handleCollision(body, parent.NW.body);
           }
         }
         if(parent.NE.body != null && parent.NE != this){
           if(dist(body.x, body.y, parent.NE.body.x, parent.NE.body.y) < 10){
             //collision
             handleCollision(body, parent.NE.body);
           }
         }
         if(parent.SW.body != null && parent.SW != this){
           if(dist(body.x, body.y, parent.SW.body.x, parent.SW.body.y) < 10){
             //collision
             handleCollision(body, parent.SW.body);
           }
         }
         if(parent.SE.body != null && parent.SE != this){
           if(dist(body.x, body.y, parent.SE.body.x, parent.SE.body.y) < 10){
             //collision
             handleCollision(body, parent.SE.body);
           }
         }
       }
    } else {
      //recursively call this function on children nodes
      if(NW != null){
        NW.detectCollisions();
        NE.detectCollisions();
        SW.detectCollisions();
        SE.detectCollisions();
      }
    }
  }
  
  void handleCollision(Body b1, Body b2){// body1 & body2
    float x = (b1.x + b2.x) / 2;
    float y = (b1.y + b2.y) / 2;
    float vx = ((b1.vx * b1.mass) + (b2.vx * b2.mass)) / (b1.mass + b2.mass);
    float vy = ((b1.vy * b1.mass) + (b2.vy * b2.mass)) / (b1.mass + b2.mass);
    float mass = b1.mass + b2.mass;
    
    Body b3 = new Body(x, y, 0, 0, mass, bodies.size());//size of bodies works out to be the correct id with the two bodies deleted later
    b3.vx = vx;
    b3.vy = vy;
    bodies.add(b3);
    bodies.remove(b1);
    bodies.remove(b2);
  }
  
  void render(){
    stroke(color(240, 30, 30));
    fill(color(0, 255, 0, 0));
    float size = quad.getSize();
    
    if(showBHTree){
      rect(quad.lowerLeft.x, quad.lowerLeft.y - size, size, size);
    }
    
    if(bodyArchive != null) {
      size = bodyArchive.mass / 3;
      ellipse(bodyArchive.x, bodyArchive.y, size, size);
    }
    
    fill(color(25, 65, 250));
    size = 20 / (1 + level);
    if(showCenterOfMass){
      ellipse(px, py, size, size);
    }
    
    if(NW != null) NW.render();
    if(NE != null) NE.render();
    if(SW != null) SW.render();
    if(SE != null) SE.render();
  }
}

void clusterSetup() {
  final int V = 3;
  
  for(int i = 0; i < count; i++){
    float x = (float)random(400, 550);
    float y = (float)random(400, 550);
    float mass = (float)random(1, 20);
    float vy = random(-V, V);
    float vx = random(-V, V);
    
    Body newBody = new Body(x, y, 0, 0, mass, i);
    newBody.vx = vx;
    newBody.vy = vy;
    bodies.add(newBody);
  }
}

void spiralSetup(){
    for(int i = 0; i < count; i++){
    
    float angle = ((float)i / (float)count) * PI * 2;
    float distance = random(0, 598);
    
    float x = 599 + cos(angle) * distance;
    float y = 599 + sin(angle) * distance;
    float mass = (float)random(1, 20);
    float vy = 0;
    float vx = 0;
    
    Body newBody = new Body(x, y, 0, 0, mass, i);
    newBody.vx = vx;
    newBody.vy = vy;
    bodies.add(newBody);
  }
}

void orbitSetup(){
    for(int i = 0; i < count; i++){
    float avgMass = 10.5;
    float totalMass = avgMass * count;
    
    float angle = ((float)i / (float)count) * 360;
    float distance = random(0, 598);
    
    float travelAngle = angle + PI / 2;
    float travelSpeed = sqrt((G * totalMass) / distance) * 35;
    println(travelSpeed);
    
    float x = 599 + cos(angle) * distance;
    float y = 599 + sin(angle) * distance;
    float mass = (float)random(1, 20);
    float vy = sin(travelAngle) * travelSpeed;
    float vx = cos(travelAngle) * travelSpeed;
    
    Body newBody = new Body(x, y, 0, 0, mass, i);
    newBody.vx = vx;
    newBody.vy = vy;
    bodies.add(newBody);
  }
}

BHTree tree;
ArrayList<Body> bodies = new ArrayList<Body>();

float G = 0.0006;

int count = 100;

boolean showBHTree = false;
boolean showCenterOfMass = false;

void setup(){
  size(1200, 1200);
  background(10, 10, 10);
  
  //clusterSetup();
  clusterSetup();
}

void draw(){
  background(10, 10, 10);
  tree = new BHTree(new Quad(new PVector(1, 1199), 1198), 0, null);
  for(int i = 0; i < bodies.size(); i++){
    if(bodies.get(i) == null) continue;
    tree.insert(bodies.get(i));
    
    tree.updateForces(bodies.get(i));
    bodies.get(i).updatePosition();
  }
  tree.render();
}

void mouseClicked(){
  float x = mouseX;
  float y = mouseY;
  Body body = new Body(x, y, 0, 0, 1, bodies.size());
  bodies.add(body);
  count += 1;
  //(count);
}

void keyPressed(){
  if(keyCode == 84){//t
    showBHTree = !showBHTree;
  }
  if(keyCode == 77){//m
    showCenterOfMass = !showCenterOfMass;
  }
}