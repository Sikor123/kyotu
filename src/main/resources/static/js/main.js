'use strict';

var stompClient = null;
var numberOfFloors = null;
var numberOfElevators = null;
var container = document.querySelector('#elevators');
var elevators = [];



async function fetchBuildingSpec() {
  try {
    const response = await fetch('http://localhost:8080/api/v1/building/spec');

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    numberOfFloors = data.numberOfFloors;
    numberOfElevators = data.numberOfElevators;
    console.log(numberOfFloors, numberOfElevators);
    buildTheBuilding();
    createElevatorsText();
  } catch (error) {
    console.error('Failed to fetch building spec:', error);
  }
}


function connect() {
        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, onConnected, onError);
}

function onConnected() {
for (let i = 0; i < numberOfElevators; i++) {
  stompClient.subscribe('/topic/elevators/'+(i+1), onMessageReceived);
}
}

function onError(error) {
    console.log('Could not connect to WebSocket server. Please refresh this page to try again!');
}

function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);
    var elevatorId = message.elevatorId;
    var elevatorContainer = document.querySelector(`#elevator-${elevatorId}`);
    if (elevatorContainer) {
      elevatorContainer.textContent =     `Elevator ${elevatorId} | Floor: ${message.currentPosition} | State: ${message.elevatorState}`;
    }
    moveElevator(elevatorId-1, message.currentPosition);



}

function createElevatorsText(){
for (let i = 0; i < numberOfElevators; i++) {
  const p = document.createElement('p');
  p.textContent = `Elevator ${i+1}`;
  p.id = `elevator-${i+1}`;
  container.appendChild(p);
}}

fetchBuildingSpec().then(connect());


var unit = 50;
var buildingHeight = null;
var buildingWidth = null;
var building = null;
var floorHeight = null;

function callElevator(floorNumber) {
  fetch(`http://localhost:8080/api/v1/elevators/call/${floorNumber}`, {
    method: "POST"
  })
    .then(response => {
      if (!response.ok) throw new Error("Network response was not ok");

    })
    .then(data => {
      console.log("Elevator called:", data);
    })
    .catch(err => {
      console.error("Error calling elevator:", err);
    });
}

function callElevatorFloor(elevatorNumber, floorNumber) {
  fetch(`http://localhost:8080/api/v1/elevators/${elevatorNumber}/floor/${floorNumber}`, {
    method: "POST"
  })
    .then(response => {
      if (!response.ok) throw new Error("Network response was not ok");
    })
    .catch(err => console.error("Error calling elevator:", err));
}



function buildTheBuilding(){
buildingHeight = 3 * numberOfFloors * unit;
buildingWidth = 2 * numberOfElevators * unit;
building = document.querySelector('#building')
building.style.height = buildingHeight + "px";
building.style.width = buildingWidth + "px";

floorHeight = buildingHeight / numberOfFloors;

  for (let floor = 1; floor < numberOfFloors; floor++) {
    const line = document.createElement("div");
    line.className = "floor-line";
    line.style.bottom = floor * floorHeight + "px";
    building.appendChild(line);
  }

  for (let floor = 0; floor < numberOfFloors; floor++) {
    const btn = document.createElement("button");
    btn.className = "floor-call-button";
    btn.innerText = `Call`;

    const floorHeight =  buildingHeight / numberOfFloors;
    btn.style.bottom = unit + floor * floorHeight + "px";

    btn.addEventListener("click", () => {
      callElevator(floor);
    });

    building.appendChild(btn);
}

const elevatorWidth = unit;
const elevatorHeight = unit*2;
for (let i = 0; i < numberOfElevators; i++) {
  const el = document.createElement("div");
  el.className = "elevator";

  el.style.width = elevatorWidth + "px";
  el.style.height = elevatorHeight + "px";

  el.style.left = (i * 2 * unit + unit / 2) + "px";

  el.style.bottom = "0px";

  building.appendChild(el);
  elevators.push(el);
}

elevators.forEach((el, elevatorIndex) => {
  const panel = document.createElement("div");
  panel.className = "elevator-panel";

  for (let floor = 0; floor < numberOfFloors; floor++) {
    const btn = document.createElement("button");
    btn.innerText = floor;
    btn.addEventListener("click", () => {
      callElevatorFloor(elevatorIndex+1, floor);
    });
    panel.appendChild(btn);
  }

  el.appendChild(panel);
});
}

function moveElevator(elevatorIndex, position) {
  if (position < 0 || position > numberOfFloors) return;

  const floorHeight = buildingHeight / numberOfFloors;
  const yPosition = position * floorHeight;

  elevators[elevatorIndex].style.bottom = yPosition + "px";
}

