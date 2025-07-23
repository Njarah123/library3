-- SQL script to update the totalBorrows field for all books based on their borrowing history

-- First, make sure all books have a non-null totalBorrows field
UPDATE books
SET total_borrows = 0
WHERE total_borrows IS NULL;

-- Then, update the totalBorrows field for each book based on the count of borrowings
UPDATE books b
SET total_borrows = (
    SELECT COUNT(*)
    FROM borrowings br
    WHERE br.book_id = b.id
);

-- Verify the update
SELECT id, title, total_borrows
FROM books
ORDER BY total_borrows DESC;