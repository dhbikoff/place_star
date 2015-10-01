# Place *
---

### Create placeholder images based on search terms

#### Built With:
- Scala 2.11
- Play Framework 2.4.3
- Google Custom Search API
- Bing Search API
- [Scrimage](https://github.com/sksamuel/scrimage) - Scala image processing library

#### Live Site:
- Hosted on [Heroku](https://guarded-reaches-5004.herokuapp.com)
- Uses free API plans, so Google results are limited to 100/day and Bing results are limited to 5000/month
- Rate limits are at the app level and not per user

#### Usage:
- uses rest routing parameters
- uses underscores to separate search terms
- supports png, jpg, jpeg extension parameters (defaults to jpg)
- request format `http://localhost:9000/:searchProvider/:search/:width/:height/:ext(optional)`

#### Examples:
- `http://localhost:9000/google/corgi/400/600/png`
- `http://localhost:9000/bing/rottweiler/500/500/jpg`
- `http://localhost:9000/bing/corgi_rottweiler_mix/800/400`

#### Notes:
- Picks a random image from the search results so you can refresh for a different image
- Results will be different from web searches due to API provider implementation
