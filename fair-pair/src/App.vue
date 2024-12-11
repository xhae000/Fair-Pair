<template>
  <div>
    <div id="map" class="fixed w-dvw h-dvh"></div>
    <div v-if="!isComputing && !isCompleted" class="fixed left-0 top-0 w-dvw" style="z-index: 999">
      <div class="flex">
        <div class="flex justify-between px-3">
          <div
            @click="resetPoints"
            style="cursor: pointer"
            class="inline-block mt-7 border-2 rounded-xl font-extrabold border-blue-500 p-5 bg-white text-blue-500"
          >
            랜덤 위치 재설정
          </div>
        </div>
        <div class="flex justify-between px-3">
          <div
            @click="setDestination"
            style="cursor: pointer"
            class="inline-block mt-7 border-2 rounded-xl font-extrabold border-blue-500 p-5 bg-blue-500 text-white"
          >
            약속장소 찾기
          </div>
        </div>
      </div>
    </div>

    <div v-if="isComputing" class="animate-fade fixed bottom-5 left-5 text-center p-5 text-2xl font-extrabold rounded-xl w-[20dvw] bg-blue-600 text-white">
      약속장소 계산 중...
    </div>
    <div v-show="isCompleted" class="fixed top-5 right-5 p-5 text-2xl font-extrabold rounded-xl w-[35dvw] border-1 border-[#c5c5c5] bg-white">
      <div class="font-bold p-3 text-xl">
        약속장소까지 걸리는 시간
      </div>
      <div class="text-blue-500 p-2">
        파란점 : {{ blueDuration }}분
      </div>
      <div class="text-red-500 p-2">
        빨간점 : {{ redDuration }}분
      </div>
      <div class="text-green-500 p-2">
        초록점 : {{ greenDuration }}분
      </div>

      <canvas class="mt-5" id="durationGraph"></canvas>

    </div>
  </div>
</template>

<script setup>
/* global google */
import { onMounted, ref } from "vue";
import { Loader } from "@googlemaps/js-api-loader";
import axios from "axios";
import Chart from "chart.js/auto";

import GreenPoint from "./assets/green-point.png";
import RedPoint from "./assets/red-point.png";
import BluePoint from "./assets/blue-point.png";
import PurpleStar from "./assets/purple-star.png";

const apiKey = "AIzaSyAe0Ow85wqOTv5fCgiNc-WT-eeqCtiNZDU";
const destination = ref({ lat: 37.25403, lng: 127.041289 });
const drawDirection = ref(false);
const isComputing = ref(false);
const isCompleted = ref(false);
const durationHistory = ref([]);

const getRandomLatLngInSuwon = () => {
  const latMin = 37.230;
  const latMax = 37.320;
  const lngMin = 126.970;
  const lngMax = 127.100;
  return { lat: Math.random() * (latMax - latMin) + latMin, lng: Math.random() * (lngMax - lngMin) + lngMin };
};

const randomPoints = ref(Array.from({ length: 3 }, getRandomLatLngInSuwon));

const resetPoints = () => {
  randomPoints.value = Array.from({ length: 3 }, getRandomLatLngInSuwon);
  drawDirection.value = false;
  isCompleted.value = false;
  loadMap();
};

const pointImage = [BluePoint, RedPoint, GreenPoint];

const loadMap = () => {
  const loader = new Loader({ apiKey, libraries: ["places"] });

  loader.load().then(() => {
    const map = new google.maps.Map(document.getElementById("map"), {
      center: destination.value,
      zoom: 12.5,
      disableDefaultUI: true,
    });

    const addMarkers = () => {
      if (drawDirection.value) {
        new google.maps.Marker({
          position: destination.value,
          map,
          icon: { url: PurpleStar, scaledSize: new google.maps.Size(35, 35) },
        });
      }

      randomPoints.value.forEach((point, index) =>
        new google.maps.Marker({
          position: point,
          map,
          icon: {
            url: pointImage[index],
            scaledSize: new google.maps.Size(25, 25),
          },
        })
      );
    };

    addMarkers();
    if (drawDirection.value) showRoutes(map);
  });
};

const color = ["#0051ff", "#ff0000", "#a2ff00"];

const showRoutes = (map) => {
  const directionsService = new google.maps.DirectionsService();

  randomPoints.value.concat(destination.value).forEach((point, index) => {
    const directionsRenderer = new google.maps.DirectionsRenderer({
      suppressMarkers: true,
      polylineOptions: { strokeColor: color[index], strokeWeight: 5 },
    });
    directionsRenderer.setMap(map);

    directionsService.route(
      {
        origin: randomPoints.value[index],
        destination: destination.value,
        travelMode: google.maps.TravelMode.TRANSIT,
      },
      (response, status) => {
        if (status === google.maps.DirectionsStatus.OK) {
          directionsRenderer.setDirections(response);
        }
      }
    );
  });
};

const blueDuration = ref(0);
const redDuration = ref(0);
const greenDuration = ref(0);

const setDestination = () => {
  isComputing.value = true;

  const params = randomPoints.value.reduce((acc, point, index) => {
    acc[`p${index + 1}x`] = point.lat;
    acc[`p${index + 1}y`] = point.lng;
    return acc;
  }, {});

  axios
    .get("http://localhost:4297/getOptimizedDestination", { params })
    .then((response) => {
      destination.value = { lat: response.data.lat, lng: response.data.lon };
      isComputing.value = false;

      blueDuration.value = response.data.blueDuration;
      redDuration.value = response.data.redDuration;
      greenDuration.value = response.data.greenDuration;

      // 문자열을 배열로 변환하여 저장
      durationHistory.value = response.data.durationHistory
        .split(" ") // 띄어쓰기를 기준으로 분리
        .map((val) => parseFloat(val)); // 문자열을 숫자로 변환      console.log(durationHistory.value)
      
        drawGraph();
        isCompleted.value = true
    })
    .catch((error) => {
      console.error("목적지 설정 중 오류:", error);
    })
    .finally(() => {
      drawDirection.value = true;
      loadMap();
    });
};

const drawGraph = () => {
  const ctx = document.getElementById("durationGraph").getContext("2d");

  new Chart(ctx, {
  type: "line",
  data: {
    labels: durationHistory.value.map((_, index) => index + 1), // Iteration 번호
    datasets: [
      {
        label: "공평성",
        data: durationHistory.value, // 원본 데이터
        borderColor: "rgba(75, 192, 192, 1)",
        backgroundColor: "rgba(75, 192, 192, 0.2)",
        borderWidth: 2,
      },
    ],
  },
  options: {
    responsive: true,
    plugins: {
      legend: {
        display: true,
        position: "top",
      },
    },
    scales: {
      x: {
        title: {
          display: true,
          text: "Iteration",
        },
      },
      y: {
        title: {
          display: true,
          text: "공평성",
        },
        reverse: true, // y축 뒤집기
      },
    },
  },
});

};

onMounted(() => {
  loadMap();
});
</script>

<style>
@keyframes fadeInOut {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
}

.animate-fade {
  animation: fadeInOut 3s ease-in-out infinite;
}
</style>
