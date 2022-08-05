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

class Body implements Cloneable{
  float x;
  float y;
  float px;
  float py;
  float mass;
  public Body(float xPos, float yPos, float pxPos, float pyPos, float m){
    x = xPos;
    y = yPos;
    px = pxPos;
    py = pyPos;
    mass = m;
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
    return new Body(0, 0, 10, 10, 10);
  }
  
}

class BHTree {
  private Body body;
  private Quad quad;
  private BHTree NW;
  private BHTree NE;
  private BHTree SW;
  private BHTree SE;
  
  private int level;
  
  //center of mass
  private float px;
  private float py;
  private float mass = 0;
  
  public BHTree(Quad q, int l){
    quad = q;
    level = l;
  }
  public void insert(Body b){
    if(body == null && NW == null){
      body = b;
      px = body.x;
      py = body.y;
      mass += body.mass;
      
    } else {
      //update center of mass
      px = (px * mass + b.x * b.mass) / (mass + b.mass);
      py = (py * mass + b.y * b.mass) / (mass + b.mass);
      mass += b.mass;
      
      Body other = null;
      if(body != null) other = new Body(body.x, body.y, body.px, body.py, body.mass); //body
      body = null;
      if (NW == null) NW = new BHTree(quad.NW(), level + 1);
      if (NE == null) NE = new BHTree(quad.NE(), level + 1);
      if (SW == null) SW = new BHTree(quad.SW(), level + 1);
      if (SE == null) SE = new BHTree(quad.SE(), level + 1);
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
  
  void updateForce(Body b){
    float theta = 0.5;
    if(NW == null && body != null){// if this is an external node
      if(b != body){
        //calculate and add force
      }
    }
    float sd = quad.getSize() / dist(b.x, b.y, body.x, body.y);
    
    if(sd < theta){
      //calculate and add force
    } else {
      NW.updateForce(b);
      NE.updateForce(b);
      SW.updateForce(b);
      SE.updateForce(b);
    }
  }
  
  PVector calculateForce(x1, y1, m1, x2, y2, m2){
    float angle = atan2(y2 - y1, x2 - x1);
    
    return 0;
  }
  
  void render(){
    stroke(color(240, 30, 30));
    fill(color(0, 255, 0, 0));
    float size = quad.getSize();
    
    if(showBHTree){
      rect(quad.lowerLeft.x, quad.lowerLeft.y - size, size, size);
    }
    
    if(body != null) {
      ellipse(body.x, body.y, .5, .5);
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

void setup(){
  size(800, 800);
  background(10, 10, 10);
}


BHTree tree;
Body[] bodies = new Body[500];
int count = 0;

boolean showBHTree = true;
boolean showCenterOfMass = true;

void draw(){
  background(10, 10, 10);
  tree = new BHTree(new Quad(new PVector(1, 799), 798), 0);
  for(int i = 0; i < count; i++){
    if(bodies[i] == null) continue;
    tree.insert(bodies[i]);
  }
  tree.render();
}

void mouseClicked(){
  float x = mouseX;
  float y = mouseY;
  Body body = new Body(x, y, 0, 0, 10);
  bodies[count] = body;
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