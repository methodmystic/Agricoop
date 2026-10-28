@echo off
git add .
git commit -m "update: deployment on netlify production"
git pull origin main --no-edit
git push origin main
npx netlify deploy --prod --dir=client
