DROP INDEX IF EXISTS itemsOrder;
DROP INDEX IF EXISTS foodTime; 

CREATE INDEX itemsOrder ON ItemsInOrder(orderID);
CREATE INDEX foodTime ON FoodOrder(login, orderTimestamp DESC);
