# RC Car WiFi Controller App

This is a mobile app for controlling a remote-controlled car via WiFi. The app allows you to drive the car and control its speed and direction in real-time.
Originally car designed for a standard RC board, it has been updated to work with an ESP32 paired with an L298M motor driver module.

# NOTE:
- This is an early version of the app; I may or may not update it in the future.
- The ping system is not very accurate. It measures the round-trip time (phone → ESP32 → phone) rather than the exact response time of the ESP32.
- High ping values can also be influenced by smartphone modem limitations. Many mobile devices have modems optimized for power efficiency and may not prioritize low-latency communication, leading to increased round-trip times.
- ESP32 code in [ESP32 Remote Car Controller](https://github.com/Peppe289/ESP32-WiFi-Car-Control) github repo.
