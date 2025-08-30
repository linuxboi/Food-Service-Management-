# Food Service Management System

A JavaFX-based application for managing food service operations, including user management, product management, order tracking, and dashboards for admins and customers.

## Features

- User registration and authentication
- Admin and customer dashboards
- Product and order management
- Database integration (SQL)
- Modern UI with FXML and CSS

## Project Structure

```
src/
  main/
    java/
      com/mycompany/mavenproject2/
        App.java
        controllers/
        models/
        services/
        utils/
    resources/
      database/
      fxml/
      styles/
  test/
    java/
```

## Setup Instructions

1. **Clone the repository:**
   ```sh
   git clone https://github.com/linuxboi/Food-Service-Management-.git
   ```
2. **Import the project into your IDE (e.g., IntelliJ IDEA, NetBeans, Eclipse).**
3. **Ensure you have Java 8+ and Maven installed.**
4. **Set up the database:**
   - Use the SQL files in `src/main/resources/database/` to initialize your database.
5. **Build and run:**
   ```sh
   mvn clean install
   mvn javafx:run
   ```

## Usage

- Register as a new user or log in.
- Admins can manage products, users, and view orders.
- Customers can browse products, place orders, and view their order history.

## Contribution

1. Fork the repository.
2. Create your feature branch (`git checkout -b feature/YourFeature`).
3. Commit your changes (`git commit -am 'Add new feature'`).
4. Push to the branch (`git push origin feature/YourFeature`).
5. Open a Pull Request.

## License

This project is licensed under the MIT License.
